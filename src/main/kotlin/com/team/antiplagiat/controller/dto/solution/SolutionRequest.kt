package com.team.antiplagiat.controller.dto.solution

import jakarta.validation.constraints.NotBlank

data class SolutionRequest(
    val problemId: Long,
    @field:NotBlank(message = "Язык программирования не может быть пустым")
    val language: String,
    @field:NotBlank(message = "Путь к файлу не может быть пустым")
    val filePath: String,
    val code: String?
)

