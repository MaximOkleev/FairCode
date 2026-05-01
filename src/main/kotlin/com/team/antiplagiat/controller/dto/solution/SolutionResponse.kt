package com.team.antiplagiat.controller.dto.solution

import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import java.time.LocalDateTime

data class SolutionResponse(
    val id: Long,
    val userId: Long,
    val problemId: Long,
    val language: String,
    val status: SolutionStatus,
    val submittedAt: LocalDateTime,
    val filePath: String,
    val code: String?
) {
    companion object {
        fun fromEntity(solution: Solution): SolutionResponse = SolutionResponse(
            id = solution.id,
            userId = solution.user.id,
            problemId = solution.problem.id,
            language = solution.language,
            status = solution.status,
            submittedAt = solution.submittedAt,
            filePath = solution.filePath,
            code = solution.code
        )
    }
}

fun SolutionRequest.toEntity(user: com.team.antiplagiat.models.User, problem: com.team.antiplagiat.models.Problem) =
    com.team.antiplagiat.models.Solution(
        user = user,
        problem = problem,
        language = this.language,
        status = SolutionStatus.WAITING,
        submittedAt = LocalDateTime.now(),
        filePath = this.filePath,
        code = this.code
    )

