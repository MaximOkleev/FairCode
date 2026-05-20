package com.team.antiplagiat.repository

import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismMatch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PlagiarismMatchRepository : JpaRepository<PlagiarismMatch, Long> {
    fun findAllByCheckRun(checkRun: PlagiarismCheckRun): List<PlagiarismMatch>

    @Query(
        """
            select m from PlagiarismMatch m
            join fetch m.firstSolution fs
            join fetch fs.user
            join fetch fs.problem
            join fetch m.secondSolution ss
            join fetch ss.user
            join fetch ss.problem
            where m.checkRun = :checkRun
        """
    )
    fun findAllByCheckRunWithSolutions(
        @Param("checkRun") checkRun: PlagiarismCheckRun
    ): List<PlagiarismMatch>
}
