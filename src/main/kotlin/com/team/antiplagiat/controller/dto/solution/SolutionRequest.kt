package com.team.antiplagiat.controller.dto.solution

data class SolutionRequest(
    val userId: Long,
    val problemId: Long,
    val language: String,
    val filePath: String,
    val code: String?
)

