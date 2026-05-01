package com.team.antiplagiat.controller.dto.problem

import com.team.antiplagiat.models.Problem

data class ProblemRequest(
    val name: String,
    val description: String?
)

fun ProblemRequest.toEntity(): Problem = Problem(
    name = this.name,
    description = this.description
)

