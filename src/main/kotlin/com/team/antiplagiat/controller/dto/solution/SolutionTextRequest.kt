package com.team.antiplagiat.controller.dto.solution

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SolutionTextRequest(
    @field:NotBlank(message = "Язык программирования не может быть пустым")
    @field:Size(max = 50, message = "Язык программирования не должен быть длиннее 50 символов")
    val language: String,

    @field:NotBlank(message = "Путь к файлу не может быть пустым")
    @field:Size(max = 500, message = "Путь к файлу не должен быть длиннее 500 символов")
    val filePath: String,

    @field:NotBlank(message = "Код не может быть пустым")
    @field:Size(max = 100000, message = "Код не должен быть длиннее 100000 символов")
    val code: String
)
