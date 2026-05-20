package com.team.antiplagiat.repository

import com.team.antiplagiat.models.ImportJob
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ImportJobRepository : JpaRepository<ImportJob, Long> {
    fun findByAdminIdOrderByCreatedAtDesc(adminId: Long, pageable: Pageable): Page<ImportJob>

    fun findByAdminIdOrderByCreatedAtDesc(adminId: Long): List<ImportJob>

    fun findByIdAndAdminId(id: Long, adminId: Long): ImportJob?
}

