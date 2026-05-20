package com.team.antiplagiat.controller.dto.contest

import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class ContestRequest(
    @field:NotBlank(message = "Название контеста не может быть пустым")
    @field:Size(max = 200, message = "Название контеста не должно быть длиннее 200 символов")
    val name: String,

    val startedAt: LocalDateTime,

    @field:Min(value = 1, message = "Продолжительность контеста должна быть минимум 1 секунда")
    val duration: Int
)

fun ContestRequest.toEntity(admin: User): Contest = Contest(
    name = this.name,
    admin = admin,
    startedAt = this.startedAt,
    duration = this.duration
)

