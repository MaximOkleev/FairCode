package com.team.antiplagiat.controller.dto.contest

import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import java.time.LocalDateTime

data class ContestRequest(
    val name: String,
    val adminId: Long,
    val startedAt: LocalDateTime,
    val duration: Int
)

fun ContestRequest.toEntity(admin: User): Contest = Contest(
    name = this.name,
    admin = admin,
    startedAt = this.startedAt,
    duration = this.duration
)

