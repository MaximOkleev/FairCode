package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.User
import com.team.antiplagiat.models.Problem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SolutionRepository : JpaRepository<Solution, Long> {
    fun findAllByUserId(userId: Long): List<Solution>
    fun countByUserAndProblem(user: User, problem: Problem): Long
    fun existsByUserAndProblemAndFilePath(user: User, problem: Problem, filePath: String): Boolean

    @Query(
        """
        select distinct s.problem.id as problemId, lower(trim(s.language)) as language
        from Solution s
        where s.code is not null and trim(s.code) <> ''
        """
    )
    fun findBucketsWithCode(): List<SolutionBucket>

    @Query(
        """
        select s
        from Solution s
        join fetch s.user
        join fetch s.problem
        where s.code is not null
          and trim(s.code) <> ''
          and s.problem.id = :problemId
          and lower(trim(s.language)) = :language
        """
    )
    fun findAllWithCodeByProblemAndLanguage(
        @Param("problemId") problemId: Long,
        @Param("language") language: String
    ): List<Solution>
}

interface SolutionBucket {
    val problemId: Long
    val language: String
}
