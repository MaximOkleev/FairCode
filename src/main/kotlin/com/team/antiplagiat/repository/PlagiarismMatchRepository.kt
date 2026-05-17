package com.team.antiplagiat.repository

import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismMatch
import org.springframework.data.jpa.repository.JpaRepository

interface PlagiarismMatchRepository : JpaRepository<PlagiarismMatch, Long> {
    fun findAllByCheckRun(checkRun: PlagiarismCheckRun): List<PlagiarismMatch>
}
