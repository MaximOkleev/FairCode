package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismCheckSummaryResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismComparisonResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismContestProblemCheckResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismMatchedFragmentResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismMatchResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismPairGroupResponse
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.PlagiarismCheckType
import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.models.PlagiarismMatch
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.plagiarism.CodePlagiarismDetector
import com.team.antiplagiat.repository.PlagiarismCheckRunRepository
import com.team.antiplagiat.repository.PlagiarismMatchRepository
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.ContestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime


@Service
class PlagiarismService(
    private val plagiarismCheckRunRepository: PlagiarismCheckRunRepository,
    private val plagiarismMatchRepository: PlagiarismMatchRepository,
    private val solutionRepository: SolutionRepository,
    private val contestRepository: ContestRepository
) {

    @Transactional
    fun compareOwnedSolutions(
        ownerId: Long,
        firstSolutionId: Long,
        secondSolutionId: Long,
        threshold: Double = CodePlagiarismDetector.DEFAULT_THRESHOLD
    ): PlagiarismComparisonResponse {
        require(firstSolutionId != secondSolutionId) { "solution ids must be different" }
        require(threshold in 0.0..1.0) { "threshold must be in range 0.0..1.0" }

        val first = findOwnedSolution(firstSolutionId, ownerId)
        val second = findOwnedSolution(secondSolutionId, ownerId)

        val comparison = compare(first, second, threshold)
        val run = saveRun(
            ownerId = ownerId,
            checkType = PlagiarismCheckType.SOLUTION_COMPARE,
            threshold = threshold,
            checkedSolutions = 2,
            comparedPairs = 1,
            matches = if (comparison.plagiarism) 1 else 0,
            groups = if (comparison.plagiarism) 1 else 0,
            solutionId = first.id.takeIf { first.id == second.id }
        )
        saveMatchedPairs(run, listOf(comparison), mapOf(first.id to first, second.id to second))

        return comparison.copy(runId = run.id)
    }

    @Transactional
    fun checkContestProblem(
        ownerId: Long,
        contestId: Long,
        problemId: Long,
        threshold: Double = CodePlagiarismDetector.DEFAULT_THRESHOLD
    ): PlagiarismContestProblemCheckResponse {
        require(threshold in 0.0..1.0) { "threshold must be in range 0.0..1.0" }

        val contest = contestRepository.findById(contestId)
            .orElseThrow { ResourceNotFoundException("Contest with id=$contestId not found") }
        if (contest.admin.id != ownerId || contest.problems.none { it.id == problemId }) {
            throw ResourceNotFoundException("Contest problem not found")
        }

        val solutions = solutionRepository.findAllWithCodeByContestAndProblem(contestId, problemId)
            .filter { it.user.id == ownerId }

        val pairs = solutions
            .groupBy { it.language.trim().lowercase() }
            .values
            .flatMap { bucket ->
                val comparisons = mutableListOf<PlagiarismComparisonResponse>()
                for (firstIndex in 0 until bucket.lastIndex) {
                    for (secondIndex in firstIndex + 1 until bucket.size) {
                        comparisons += compare(bucket[firstIndex], bucket[secondIndex], threshold)
                    }
                }
                comparisons
            }

        val groups = buildPairGroups(pairs)
        val run = saveRun(
            ownerId = ownerId,
            checkType = PlagiarismCheckType.CONTEST_PROBLEM,
            threshold = threshold,
            checkedSolutions = solutions.size,
            comparedPairs = pairs.size,
            matches = pairs.count { it.plagiarism },
            groups = groups.size,
            contestId = contestId,
            problemId = problemId
        )
        saveMatchedPairs(run, pairs, solutions.associateBy { it.id })

        return PlagiarismContestProblemCheckResponse(
            runId = run.id,
            checkedSolutions = solutions.size,
            comparedPairs = pairs.size,
            matches = pairs.count { it.plagiarism },
            threshold = threshold,
            pairs = pairs,
            groups = groups
        )
    }

    @Transactional(readOnly = true)
    fun findHistory(ownerId: Long): List<PlagiarismCheckSummaryResponse> =
        plagiarismCheckRunRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)
            .map { PlagiarismCheckSummaryResponse.fromEntity(it) }

    @Transactional(readOnly = true)
    fun findHistoryMatches(ownerId: Long, runId: Long): List<PlagiarismMatchResponse> {
        val run = plagiarismCheckRunRepository.findById(runId)
            .filter { it.ownerId == ownerId }
            .orElseThrow { ResourceNotFoundException("Plagiarism check run with id=$runId not found") }

        return plagiarismMatchRepository.findAllByCheckRunWithSolutions(run)
            .map { PlagiarismMatchResponse.fromEntity(it) }
    }

    private fun findOwnedSolution(solutionId: Long, ownerId: Long) =
        solutionRepository.findByIdWithRelations(solutionId)
            ?.takeIf { it.user.id == ownerId }
            ?: throw ResourceNotFoundException("Solution with id=$solutionId not found")

    private fun compare(
        first: Solution,
        second: Solution,
        threshold: Double
    ): PlagiarismComparisonResponse {
        val result = CodePlagiarismDetector.check(
            firstCode = first.code.orEmpty(),
            secondCode = second.code.orEmpty(),
            threshold = threshold
        )

        val sameProblemId = first.problem.id.takeIf { it == second.problem.id }
        val sameContestId = first.contest?.id?.takeIf { it == second.contest?.id }
        val sameLanguage = first.language.takeIf { it.trim().equals(second.language.trim(), ignoreCase = true) }

        return PlagiarismComparisonResponse(
            firstSolutionId = first.id,
            secondSolutionId = second.id,
            problemId = sameProblemId,
            contestId = sameContestId,
            language = sameLanguage,
            similarity = result.similarity,
            threshold = result.threshold,
            plagiarism = result.isPlagiarism,
            matchedFragments = result.matchedFragments.map {
                PlagiarismMatchedFragmentResponse(
                    firstStartLine = it.firstStartLine,
                    firstEndLine = it.firstEndLine,
                    secondStartLine = it.secondStartLine,
                    secondEndLine = it.secondEndLine
                )
            }
        )
    }

    private fun saveRun(
        ownerId: Long,
        checkType: PlagiarismCheckType,
        threshold: Double,
        checkedSolutions: Int,
        comparedPairs: Int,
        matches: Int,
        groups: Int,
        contestId: Long? = null,
        problemId: Long? = null,
        solutionId: Long? = null
    ): PlagiarismCheckRun =
        plagiarismCheckRunRepository.save(
            PlagiarismCheckRun(
                threshold = threshold,
                checkType = checkType,
                ownerId = ownerId,
                contestId = contestId,
                problemId = problemId,
                solutionId = solutionId,
                status = PlagiarismCheckRunStatus.COMPLETED,
                checkedSolutions = checkedSolutions,
                comparedPairs = comparedPairs,
                matches = matches,
                groups = groups,
                startedAt = LocalDateTime.now(),
                finishedAt = LocalDateTime.now()
            )
        )

    private fun saveMatchedPairs(
        run: PlagiarismCheckRun,
        pairs: List<PlagiarismComparisonResponse>,
        solutionsById: Map<Long, Solution>
    ) {
        val matches = pairs
            .filter { it.plagiarism }
            .mapNotNull { pair ->
                val first = solutionsById[pair.firstSolutionId]
                val second = solutionsById[pair.secondSolutionId]
                if (first == null || second == null) {
                    null
                } else {
                    val ordered = listOf(first, second).sortedBy { it.id }
                    PlagiarismMatch(
                        checkRun = run,
                        firstSolution = ordered[0],
                        secondSolution = ordered[1],
                        similarity = pair.similarity,
                        threshold = pair.threshold,
                        detectedAt = LocalDateTime.now()
                    )
                }
            }

        if (matches.isNotEmpty()) {
            plagiarismMatchRepository.saveAll(matches)
        }
    }

    private fun buildPairGroups(pairs: List<PlagiarismComparisonResponse>): List<PlagiarismPairGroupResponse> {
        val matchedPairs = pairs.filter { it.plagiarism }
        if (matchedPairs.isEmpty()) {
            return emptyList()
        }

        val ids = matchedPairs
            .flatMap { listOf(it.firstSolutionId, it.secondSolutionId) }
            .toSet()
        val dsu = PairDisjointSet(ids)

        matchedPairs.forEach {
            dsu.union(it.firstSolutionId, it.secondSolutionId)
        }

        return dsu.groups()
            .values
            .mapNotNull { solutionIds ->
                if (solutionIds.size < 2) {
                    return@mapNotNull null
                }

                val solutionIdSet = solutionIds.toSet()
                val groupPairs = matchedPairs
                    .filter { it.firstSolutionId in solutionIdSet && it.secondSolutionId in solutionIdSet }
                    .sortedByDescending { it.similarity }
                val firstPair = groupPairs.first()

                PlagiarismPairGroupResponse(
                    groupId = solutionIds.min(),
                    solutionIds = solutionIds.sorted(),
                    problemId = firstPair.problemId,
                    contestId = firstPair.contestId,
                    language = firstPair.language,
                    maxSimilarity = groupPairs.maxOf { it.similarity },
                    pairs = groupPairs
                )
            }
            .sortedWith(compareByDescending<PlagiarismPairGroupResponse> { it.maxSimilarity }.thenBy { it.groupId })
    }

    private class PairDisjointSet(ids: Collection<Long>) {
        private val parent = ids.associateWith { it }.toMutableMap()
        private val size = ids.associateWith { 1 }.toMutableMap()

        fun find(id: Long): Long {
            var current = parent[id] ?: error("Unknown DSU node: $id")

            while (parent[current] != current) {
                current = parent[current] ?: error("Unknown DSU node: $current")
            }

            val root = current
            current = id
            while (parent[current] != current) {
                val next = parent[current] ?: error("Unknown DSU node: $current")
                parent[current] = root
                current = next
            }

            return root
        }

        fun union(firstId: Long, secondId: Long) {
            var firstRoot = find(firstId)
            var secondRoot = find(secondId)

            if (firstRoot == secondRoot) {
                return
            }

            if (size.getValue(firstRoot) < size.getValue(secondRoot)) {
                val tmp = firstRoot
                firstRoot = secondRoot
                secondRoot = tmp
            }

            parent[secondRoot] = firstRoot
            size[firstRoot] = size.getValue(firstRoot) + size.getValue(secondRoot)
        }

        fun groups(): Map<Long, List<Long>> =
            parent.keys.groupBy { find(it) }
    }
}
