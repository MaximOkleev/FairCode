package com.team.antiplagiat.controller.dto.contest

import com.team.antiplagiat.models.Contest
import java.time.LocalDateTime

data class ContestResponse(
    val id: Long,
    val name: String,
    val adminId: Long,
    val startedAt: LocalDateTime,
    val duration: Int
) {
    companion object {
        fun fromEntity(contest: Contest): ContestResponse = ContestResponse(
            id = contest.id,
            name = contest.name,
            adminId = contest.admin.id,
            startedAt = contest.startedAt,
            duration = contest.duration
        )
    }
}

