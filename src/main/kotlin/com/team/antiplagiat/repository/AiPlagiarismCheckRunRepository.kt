package com.team.antiplagiat.repository

import com.team.antiplagiat.models.AiPlagiarismCheckRun
import org.springframework.data.jpa.repository.JpaRepository

interface AiPlagiarismCheckRunRepository : JpaRepository<AiPlagiarismCheckRun, Long> {
    fun findAllByOwnerIdOrderByCreatedAtDesc(ownerId: Long): List<AiPlagiarismCheckRun>
}
