package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.Problem

data class ProblemRequest(
    val name: String,
    val description: String?,
    val condition: String? = null
)

data class ProblemResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val condition: String?
) {
    companion object {
        fun fromEntity(problem: Problem): ProblemResponse = ProblemResponse(
            id = problem.id,
            name = problem.name,
            description = problem.description,
            condition = problem.condition
        )
    }
}

fun ProblemRequest.toEntity(): Problem = Problem(
    name = this.name,
    description = this.description,
    condition = this.condition
)
