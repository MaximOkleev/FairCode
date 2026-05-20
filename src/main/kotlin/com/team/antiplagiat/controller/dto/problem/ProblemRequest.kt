package com.team.antiplagiat.controller.dto.problem

import com.team.antiplagiat.models.Problem
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProblemRequest(
    @field:NotBlank(message = "Название задачи не может быть пустым")
    @field:Size(max = 200, message = "Название задачи не должно быть длиннее 200 символов")
    val name: String,

    @field:Size(max = 5000, message = "Описание слишком длинное")
    val description: String?
)

fun ProblemRequest.toEntity(): Problem = Problem(
    name = this.name,
    description = this.description
)

