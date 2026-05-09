package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank(message = "Token не может быть пустым")
    val token: String,

    @field:NotBlank(message = "Новый пароль не может быть пустым")
    val newPassword: String
)
