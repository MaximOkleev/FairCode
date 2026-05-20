package com.team.antiplagiat.repository

import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import org.springframework.data.jpa.repository.JpaRepository

interface PlagiarismCheckRunRepository : JpaRepository<PlagiarismCheckRun, Long> {
    fun findFirstByStatusOrderByFinishedAtDesc(status: PlagiarismCheckRunStatus): PlagiarismCheckRun?
}
