package com.team.antiplagiat.controller.dto.plagiarism

import com.team.antiplagiat.models.PlagiarismMatch
import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.models.Solution
import java.time.LocalDateTime

data class PlagiarismCheckSummaryResponse(
    val runId: Long,
    val status: PlagiarismCheckRunStatus,
    val checkedSolutions: Int,
    val comparedPairs: Int,
    val matches: Int,
    val groups: Int,
    val threshold: Double,
    val errorMessage: String?,
    val createdAt: LocalDateTime,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?
) {
    companion object {
        fun fromEntity(run: PlagiarismCheckRun): PlagiarismCheckSummaryResponse =
            PlagiarismCheckSummaryResponse(
                runId = run.id,
                status = run.status,
                checkedSolutions = run.checkedSolutions,
                comparedPairs = run.comparedPairs,
                matches = run.matches,
                groups = run.groups,
                threshold = run.threshold,
                errorMessage = run.errorMessage,
                createdAt = run.createdAt,
                startedAt = run.startedAt,
                finishedAt = run.finishedAt
            )
    }
}

data class PlagiarismGroupResponse(
    val groupId: Long,
    val problemId: Long,
    val language: String,
    val maxSimilarity: Double,
    val users: List<PlagiarismUserMemberResponse>,
    val members: List<PlagiarismSolutionMemberResponse>,
    val matches: List<PlagiarismMatchResponse>
)

data class PlagiarismUserMemberResponse(
    val userId: Long,
    val login: String
) {
    companion object {
        fun fromEntity(solution: Solution): PlagiarismUserMemberResponse =
            PlagiarismUserMemberResponse(
                userId = solution.user.id,
                login = solution.user.login
            )
    }
}

data class PlagiarismSolutionMemberResponse(
    val solutionId: Long,
    val userId: Long,
    val login: String,
    val problemId: Long,
    val language: String,
    val filePath: String
) {
    companion object {
        fun fromEntity(solution: Solution): PlagiarismSolutionMemberResponse =
            PlagiarismSolutionMemberResponse(
                solutionId = solution.id,
                userId = solution.user.id,
                login = solution.user.login,
                problemId = solution.problem.id,
                language = solution.language,
                filePath = solution.filePath
            )
    }
}

data class PlagiarismMatchResponse(
    val id: Long,
    val firstSolutionId: Long,
    val firstUserId: Long,
    val secondSolutionId: Long,
    val secondUserId: Long,
    val similarity: Double,
    val threshold: Double,
    val detectedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(match: PlagiarismMatch): PlagiarismMatchResponse =
            PlagiarismMatchResponse(
                id = match.id,
                firstSolutionId = match.firstSolution.id,
                firstUserId = match.firstSolution.user.id,
                secondSolutionId = match.secondSolution.id,
                secondUserId = match.secondSolution.user.id,
                similarity = match.similarity,
                threshold = match.threshold,
                detectedAt = match.detectedAt
            )
    }
}
