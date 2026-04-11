package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.Solution
import java.time.LocalDateTime

data class NewSolutionResponse(
    val solutionId: Long
)

data class NewSolutionRequest(
    val userId: Long,
    val taskId: Long,
    val language: String,
    val filePath: String
)

fun NewSolutionRequest.toEntity(): Solution {
    return Solution(
        id = 0,
        userId = this.userId,
        taskId = this.taskId,
        language = this.language,
        filePath = this.filePath,
        date = LocalDateTime.now()
    )
}

data class GetSolutionResponse(
    val id: Long,
    val userId: Long,
    val taskId: Long,
    val language: String,
    val filePath: String,
    val createdAt: LocalDateTime
)

fun Solution.toResponse() = GetSolutionResponse(
    id = this.id,
    userId = this.userId,
    taskId = this.taskId,
    language = this.language,
    filePath = this.filePath,
    createdAt = this.date
)

data class GetCountResponse(
    val count: Int
)