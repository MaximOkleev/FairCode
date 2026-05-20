package com.team.antiplagiat.controller.dto.ai

import com.team.antiplagiat.models.AiGeneratedSolution
import com.team.antiplagiat.models.AiGeneratedSolutionStatus
import com.team.antiplagiat.models.AiProvider
import java.time.LocalDateTime

data class AiGeneratedSolutionResponse(
    val id: Long,
    val problemId: Long,
    val provider: AiProvider,
    val language: String,
    val modelName: String,
    val status: AiGeneratedSolutionStatus,
    val code: String?,
    val errorMessage: String?,
    val generatedAt: LocalDateTime
) {
    companion object {
        fun fromEntity(solution: AiGeneratedSolution): AiGeneratedSolutionResponse = AiGeneratedSolutionResponse(
            id = solution.id,
            problemId = solution.problem.id,
            provider = solution.provider,
            language = solution.language,
            modelName = solution.modelName,
            status = solution.status,
            code = solution.code,
            errorMessage = solution.errorMessage,
            generatedAt = solution.generatedAt
        )
    }
}
