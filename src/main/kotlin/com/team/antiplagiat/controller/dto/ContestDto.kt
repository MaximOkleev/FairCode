package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import java.time.LocalDateTime

data class ContestRequest(
    val name: String,
    val adminId: Long,
    val startedAt: LocalDateTime,
    val duration: Int
)

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

fun ContestRequest.toEntity(admin: User): Contest = Contest(
    name = this.name,
    admin = admin,
    startedAt = this.startedAt,
    duration = this.duration
)
