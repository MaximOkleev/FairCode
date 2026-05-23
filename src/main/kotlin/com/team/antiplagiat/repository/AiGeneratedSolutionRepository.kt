package com.team.antiplagiat.repository

import com.team.antiplagiat.models.AiGeneratedSolution
import com.team.antiplagiat.models.AiGeneratedSolutionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AiGeneratedSolutionRepository : JpaRepository<AiGeneratedSolution, Long> {
    fun findAllByProblemIdOrderByGeneratedAtDesc(problemId: Long): List<AiGeneratedSolution>

    @Query(
        """
        select a
        from AiGeneratedSolution a
        join fetch a.problem
        where a.problem.id = :problemId
          and lower(trim(a.language)) = lower(trim(:language))
        order by a.generatedAt desc
        """
    )
    fun findAllByProblemAndNormalizedLanguage(
        @Param("problemId") problemId: Long,
        @Param("language") language: String
    ): List<AiGeneratedSolution>

    @Query(
        """
        select a
        from AiGeneratedSolution a
        join fetch a.problem
        where a.problem.id = :problemId
          and a.status = :status
          and a.code is not null
          and trim(a.code) <> ''
          and lower(trim(a.language)) = lower(trim(:language))
        order by a.generatedAt desc
        """
    )
    fun findComparableByProblemAndLanguage(
        @Param("problemId") problemId: Long,
        @Param("language") language: String,
        @Param("status") status: AiGeneratedSolutionStatus = AiGeneratedSolutionStatus.SUCCESS
    ): List<AiGeneratedSolution>
}
