package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class VerifyEmailRequest(
    @field:NotBlank(message = "Токен не может быть пустым")
    val token: String
)

data class VerifyEmailResponse(
    val message: String,
    val success: Boolean,
    val userId: Long? = null,
    val token: String? = null
)

