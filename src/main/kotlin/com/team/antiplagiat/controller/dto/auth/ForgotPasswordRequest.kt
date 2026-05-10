package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class ForgotPasswordRequest(
    @field:NotBlank(message = "Email не может быть пустым")
    val email: String
)
