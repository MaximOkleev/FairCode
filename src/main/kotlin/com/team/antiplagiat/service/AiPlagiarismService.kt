package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.ai.AiPlagiarismCheckResponse
import com.team.antiplagiat.controller.dto.ai.AiPlagiarismCheckSummaryResponse
import com.team.antiplagiat.controller.dto.ai.AiPlagiarismMatchResponse
import com.team.antiplagiat.controller.dto.ai.AiPlagiarismMatchedFragmentResponse
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.AiGeneratedSolutionStatus
import com.team.antiplagiat.models.AiGeneratedSolution
import com.team.antiplagiat.models.AiPlagiarismCheckRun
import com.team.antiplagiat.models.AiPlagiarismMatch
import com.team.antiplagiat.models.AiProvider
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.plagiarism.CodePlagiarismDetector
import com.team.antiplagiat.repository.AiGeneratedSolutionRepository
import com.team.antiplagiat.repository.AiPlagiarismCheckRunRepository
import com.team.antiplagiat.repository.AiPlagiarismMatchRepository
import com.team.antiplagiat.repository.ContestRepository
import com.team.antiplagiat.repository.SolutionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AiPlagiarismService(
    private val solutionRepository: SolutionRepository,
    private val contestRepository: ContestRepository,
    private val aiGeneratedSolutionRepository: AiGeneratedSolutionRepository,
    private val aiGeneratedSolutionService: AiGeneratedSolutionService,
    private val aiPlagiarismCheckRunRepository: AiPlagiarismCheckRunRepository,
    private val aiPlagiarismMatchRepository: AiPlagiarismMatchRepository
) {

    @Transactional
    fun checkSolution(
        ownerId: Long,
        solutionId: Long,
        threshold: Double = CodePlagiarismDetector.DEFAULT_THRESHOLD
    ): AiPlagiarismCheckResponse {
        require(threshold in 0.0..1.0) { "threshold must be in range 0.0..1.0" }

        val solution = solutionRepository.findByIdWithRelations(solutionId)
            ?.takeIf { it.user.id == ownerId }
            ?: throw ResourceNotFoundException("Solution with id=$solutionId not found")

        return buildResponse(
            ownerId = ownerId,
            contestId = solution.contest?.id,
            solutionId = solution.id,
            solutions = listOf(solution),
            threshold = threshold
        )
    }

    @Transactional
    fun checkContest(
        ownerId: Long,
        contestId: Long,
        threshold: Double = CodePlagiarismDetector.DEFAULT_THRESHOLD
    ): AiPlagiarismCheckResponse {
        require(threshold in 0.0..1.0) { "threshold must be in range 0.0..1.0" }

        val contest = contestRepository.findById(contestId)
            .orElseThrow { ResourceNotFoundException("Contest with id=$contestId not found") }
        if (contest.admin.id != ownerId) {
            throw ResourceNotFoundException("Contest with id=$contestId not found")
        }

        return buildResponse(
            ownerId = ownerId,
            contestId = contestId,
            solutionId = null,
            solutions = solutionRepository.findAllByUserIdAndContestId(ownerId, contestId),
            threshold = threshold
        )
    }

    @Transactional(readOnly = true)
    fun findHistory(ownerId: Long): List<AiPlagiarismCheckSummaryResponse> =
        aiPlagiarismCheckRunRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)
            .map { AiPlagiarismCheckSummaryResponse.fromEntity(it) }

    @Transactional(readOnly = true)
    fun findHistoryRun(ownerId: Long, runId: Long): AiPlagiarismCheckResponse {
        val run = aiPlagiarismCheckRunRepository.findById(runId)
            .filter { it.ownerId == ownerId }
            .orElseThrow { ResourceNotFoundException("AI plagiarism check run with id=$runId not found") }

        val pairs = aiPlagiarismMatchRepository.findAllByCheckRunWithRelations(run)
            .map { match ->
                AiPlagiarismMatchResponse(
                    solutionId = match.solution.id,
                    contestId = match.solution.contest?.id,
                    problemId = match.solution.problem.id,
                    language = match.solution.language,
                    filePath = match.solution.filePath,
                    aiSolutionId = match.aiSolution.id,
                    provider = match.aiSolution.provider,
                    modelName = match.aiSolution.modelName,
                    similarity = match.similarity,
                    plagiarism = true,
                    matchedFragments = emptyList()
                )
            }

        return AiPlagiarismCheckResponse(
            runId = run.id,
            checkedSolutions = run.checkedSolutions,
            comparedPairs = run.comparedPairs,
            matches = run.matches,
            threshold = run.threshold,
            generatedAiSolutions = run.generatedAiSolutions,
            pairs = pairs
        )
    }

    private fun buildResponse(
        ownerId: Long,
        contestId: Long?,
        solutionId: Long?,
        solutions: List<Solution>,
        threshold: Double
    ): AiPlagiarismCheckResponse {
        val pairs = mutableListOf<AiPlagiarismMatchResponse>()
        val aiSolutionsById = mutableMapOf<Long, AiGeneratedSolution>()
        val solutionsById = solutions.associateBy { it.id }
        val generationAttempted = mutableSetOf<Pair<Long, String>>()
        var comparedPairs = 0
        var generatedAiSolutions = 0

        solutions
            .filter { !it.code.isNullOrBlank() }
            .forEach { solution ->
                var aiSolutions = aiGeneratedSolutionRepository.findComparableByProblemAndLanguage(
                    problemId = solution.problem.id,
                    language = solution.language
                )
                val generationKey = solution.problem.id to solution.language.trim().lowercase()
                val existingProviders = aiSolutions.map { it.provider }.toSet()
                if (
                    existingProviders.isNotEmpty() &&
                    existingProviders.size < AiProvider.entries.size &&
                    generationAttempted.add(generationKey)
                ) {
                    generatedAiSolutions += aiGeneratedSolutionService
                        .generateForProblem(solution.problem.id, solution.language)
                        .count { it.status == AiGeneratedSolutionStatus.SUCCESS && !it.code.isNullOrBlank() }
                    aiSolutions = aiGeneratedSolutionRepository.findComparableByProblemAndLanguage(
                        problemId = solution.problem.id,
                        language = solution.language
                    )
                }

                aiSolutions.forEach { aiSolution ->
                    aiSolutionsById[aiSolution.id] = aiSolution
                    comparedPairs++
                    val result = CodePlagiarismDetector.check(
                        firstCode = solution.code.orEmpty(),
                        secondCode = aiSolution.code.orEmpty(),
                        threshold = threshold
                    )

                    pairs += toMatchResponse(
                        solution = solution,
                        aiSolution = aiSolution,
                        similarity = result.similarity,
                        plagiarism = result.isPlagiarism,
                        matchedFragments = result.matchedFragments.map {
                            AiPlagiarismMatchedFragmentResponse(
                                solutionStartLine = it.firstStartLine,
                                solutionEndLine = it.firstEndLine,
                                aiSolutionStartLine = it.secondStartLine,
                                aiSolutionEndLine = it.secondEndLine
                            )
                        }
                    )
                }
            }

        val matches = pairs.filter { it.plagiarism }
        val run = saveRun(
            ownerId = ownerId,
            contestId = contestId,
            solutionId = solutionId,
            threshold = threshold,
            checkedSolutions = solutions.size,
            comparedPairs = comparedPairs,
            matches = matches.size,
            generatedAiSolutions = generatedAiSolutions
        )
        saveMatches(run, matches, solutionsById, aiSolutionsById)

        return AiPlagiarismCheckResponse(
            runId = run.id,
            checkedSolutions = solutions.size,
            comparedPairs = comparedPairs,
            matches = matches.size,
            threshold = threshold,
            generatedAiSolutions = generatedAiSolutions,
            pairs = pairs
        )
    }

    private fun toMatchResponse(
        solution: Solution,
        aiSolution: AiGeneratedSolution,
        similarity: Double,
        plagiarism: Boolean,
        matchedFragments: List<AiPlagiarismMatchedFragmentResponse>
    ): AiPlagiarismMatchResponse =
        AiPlagiarismMatchResponse(
            solutionId = solution.id,
            contestId = solution.contest?.id,
            problemId = solution.problem.id,
            language = solution.language,
            filePath = solution.filePath,
            aiSolutionId = aiSolution.id,
            provider = aiSolution.provider,
            modelName = aiSolution.modelName,
            similarity = similarity,
            plagiarism = plagiarism,
            matchedFragments = if (plagiarism) matchedFragments else emptyList()
        )

    private fun saveRun(
        ownerId: Long,
        contestId: Long?,
        solutionId: Long?,
        threshold: Double,
        checkedSolutions: Int,
        comparedPairs: Int,
        matches: Int,
        generatedAiSolutions: Int
    ): AiPlagiarismCheckRun =
        aiPlagiarismCheckRunRepository.save(
            AiPlagiarismCheckRun(
                ownerId = ownerId,
                contestId = contestId,
                solutionId = solutionId,
                threshold = threshold,
                status = PlagiarismCheckRunStatus.COMPLETED,
                checkedSolutions = checkedSolutions,
                comparedPairs = comparedPairs,
                matches = matches,
                generatedAiSolutions = generatedAiSolutions,
                finishedAt = LocalDateTime.now()
            )
        )

    private fun saveMatches(
        run: AiPlagiarismCheckRun,
        pairs: List<AiPlagiarismMatchResponse>,
        solutionsById: Map<Long, Solution>,
        aiSolutionsById: Map<Long, AiGeneratedSolution>
    ) {
        val matches = pairs.mapNotNull { pair ->
            val solution = solutionsById[pair.solutionId]
            val aiSolution = aiSolutionsById[pair.aiSolutionId]
            if (solution == null || aiSolution == null) {
                null
            } else {
                AiPlagiarismMatch(
                    checkRun = run,
                    solution = solution,
                    aiSolution = aiSolution,
                    similarity = pair.similarity,
                    threshold = run.threshold,
                    detectedAt = LocalDateTime.now()
                )
            }
        }

        if (matches.isNotEmpty()) {
            aiPlagiarismMatchRepository.saveAll(matches)
        }
    }

}
