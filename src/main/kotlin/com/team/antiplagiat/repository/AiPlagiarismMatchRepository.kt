package com.team.antiplagiat.repository

import com.team.antiplagiat.models.AiPlagiarismCheckRun
import com.team.antiplagiat.models.AiPlagiarismMatch
import com.team.antiplagiat.models.AiGeneratedSolution
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AiPlagiarismMatchRepository : JpaRepository<AiPlagiarismMatch, Long> {
    fun deleteAllByAiSolution(aiSolution: AiGeneratedSolution)

    @Query(
        """
        select m from AiPlagiarismMatch m
        join fetch m.solution s
        join fetch s.problem
        left join fetch s.contest
        join fetch m.aiSolution a
        where m.checkRun = :checkRun
        order by m.similarity desc, m.id
        """
    )
    fun findAllByCheckRunWithRelations(@Param("checkRun") checkRun: AiPlagiarismCheckRun): List<AiPlagiarismMatch>
}
