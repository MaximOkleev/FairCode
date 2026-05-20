package com.team.antiplagiat.repository

import com.team.antiplagiat.models.AiGeneratedSolution
import org.springframework.data.jpa.repository.JpaRepository

interface AiGeneratedSolutionRepository : JpaRepository<AiGeneratedSolution, Long> {
    fun findAllByProblemIdOrderByGeneratedAtDesc(problemId: Long): List<AiGeneratedSolution>
}
