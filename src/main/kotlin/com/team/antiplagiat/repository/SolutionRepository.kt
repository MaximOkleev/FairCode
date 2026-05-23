package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.User
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Contest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SolutionRepository : JpaRepository<Solution, Long> {
    fun findAllByUserId(userId: Long): List<Solution>
    fun findAllByUserIdAndContestId(userId: Long, contestId: Long): List<Solution>
    fun countByUserAndProblem(user: User, problem: Problem): Long
    fun countByUserAndContestAndProblem(user: User, contest: Contest, problem: Problem): Long
    fun existsByUserAndProblemAndFilePath(user: User, problem: Problem, filePath: String): Boolean
    fun existsByUserAndContestAndProblemAndFilePath(
        user: User,
        contest: Contest,
        problem: Problem,
        filePath: String
    ): Boolean
    fun deleteAllByContest(contest: Contest)
    fun deleteAllByContestAndProblem(contest: Contest, problem: Problem)
    fun deleteAllByProblem(problem: Problem)

    @Query(
        """
        select s
        from Solution s
        join fetch s.user
        join fetch s.problem
        left join fetch s.contest
        where s.id = :id
        """
    )
    fun findByIdWithRelations(@Param("id") id: Long): Solution?

    @Query(
        """
        select s
        from Solution s
        join fetch s.user
        join fetch s.problem
        join fetch s.contest c
        where c.id = :contestId
          and s.problem.id = :problemId
          and s.code is not null
          and trim(s.code) <> ''
        order by lower(trim(s.language)), s.id
        """
    )
    fun findAllWithCodeByContestAndProblem(
        @Param("contestId") contestId: Long,
        @Param("problemId") problemId: Long
    ): List<Solution>

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
