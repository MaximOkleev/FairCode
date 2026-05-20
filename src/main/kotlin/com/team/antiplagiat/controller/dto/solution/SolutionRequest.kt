package com.team.antiplagiat.controller.dto.solution

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SolutionRequest(
    @field:Min(value = 1, message = "ID задачи должен быть положительным числом")
    val problemId: Long,

    @field:NotBlank(message = "Язык программирования не может быть пустым")
    @field:Size(max = 50, message = "Язык программирования не должен быть длиннее 50 символов")
    val language: String,

    @field:NotBlank(message = "Путь к файлу не может быть пустым")
    @field:Size(max = 500, message = "Путь к файлу не должен быть длиннее 500 символов")
    val filePath: String,

    @field:Size(max = 100000, message = "Код не должен быть длиннее 100000 символов")
    val code: String?
)

