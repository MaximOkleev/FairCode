package com.team.antiplagiat.service

import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.models.PlagiarismMatch
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.plagiarism.CodePlagiarismDetector
import com.team.antiplagiat.repository.PlagiarismCheckRunRepository
import com.team.antiplagiat.repository.PlagiarismMatchRepository
import com.team.antiplagiat.repository.SolutionRepository
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PlagiarismCheckRunner(
    private val solutionRepository: SolutionRepository,
    private val plagiarismCheckRunRepository: PlagiarismCheckRunRepository,
    private val plagiarismMatchRepository: PlagiarismMatchRepository
) {

    @Async
    @Transactional
    fun run(runId: Long) {
        val run = plagiarismCheckRunRepository.findById(runId)
            .orElseThrow { IllegalArgumentException("Plagiarism check run $runId not found") }

        run.status = PlagiarismCheckRunStatus.PROCESSING
        run.startedAt = LocalDateTime.now()
        plagiarismCheckRunRepository.saveAndFlush(run)

        try {
            val detectedAt = LocalDateTime.now()
            val matches = mutableListOf<PlagiarismMatch>()
            var checkedSolutions = 0
            var comparedPairs = 0

            solutionRepository.findBucketsWithCode().forEach { bucket ->
                val bucketSolutions = solutionRepository.findAllWithCodeByProblemAndLanguage(
                    problemId = bucket.problemId,
                    language = bucket.language
                )
                checkedSolutions += bucketSolutions.size

                for (firstIndex in 0 until bucketSolutions.lastIndex) {
                    for (secondIndex in firstIndex + 1 until bucketSolutions.size) {
                        val firstSolution = bucketSolutions[firstIndex]
                        val secondSolution = bucketSolutions[secondIndex]

                        if (firstSolution.user.id == secondSolution.user.id) {
                            continue
                        }

                        comparedPairs++
                        val result = CodePlagiarismDetector.check(
                            firstCode = firstSolution.code.orEmpty(),
                            secondCode = secondSolution.code.orEmpty(),
                            threshold = run.threshold
                        )

                        if (result.isPlagiarism) {
                            matches += createMatch(run, firstSolution, secondSolution, result.similarity, detectedAt)
                        }
                    }
                }
            }

            val savedMatches = plagiarismMatchRepository.saveAll(matches).toList()
            run.checkedSolutions = checkedSolutions
            run.comparedPairs = comparedPairs
            run.matches = savedMatches.size
            run.groups = PlagiarismGrouping.buildGroups(savedMatches).size
            run.status = PlagiarismCheckRunStatus.COMPLETED
            run.finishedAt = LocalDateTime.now()
            plagiarismCheckRunRepository.save(run)
        } catch (ex: Exception) {
            run.status = PlagiarismCheckRunStatus.FAILED
            run.errorMessage = ex.message
            run.finishedAt = LocalDateTime.now()
            plagiarismCheckRunRepository.save(run)
            throw ex
        }
    }

    private fun createMatch(
        run: com.team.antiplagiat.models.PlagiarismCheckRun,
        firstSolution: Solution,
        secondSolution: Solution,
        similarity: Double,
        detectedAt: LocalDateTime
    ): PlagiarismMatch {
        val orderedPair = listOf(firstSolution, secondSolution).sortedBy { it.id }

        return PlagiarismMatch(
            checkRun = run,
            firstSolution = orderedPair[0],
            secondSolution = orderedPair[1],
            similarity = similarity,
            threshold = run.threshold,
            detectedAt = detectedAt
        )
    }
}
