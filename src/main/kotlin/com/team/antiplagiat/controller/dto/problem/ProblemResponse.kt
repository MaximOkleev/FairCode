package com.team.antiplagiat.controller.dto.problem

import com.team.antiplagiat.models.Problem

data class ProblemResponse(
    val id: Long,
    val name: String,
    val description: String?
) {
    companion object {
        fun fromEntity(problem: Problem): ProblemResponse = ProblemResponse(
            id = problem.id,
            name = problem.name,
            description = problem.description
        )
    }
}

