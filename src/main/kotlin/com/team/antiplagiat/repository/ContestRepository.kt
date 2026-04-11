package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Contest
import org.springframework.data.jpa.repository.JpaRepository

interface ContestRepository : JpaRepository<Contest, Long> {
    fun findAllByAdminId(adminId: Long): List<Contest>
}