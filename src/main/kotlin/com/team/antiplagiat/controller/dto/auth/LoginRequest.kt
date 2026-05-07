package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "Login не может быть пустым")
    val login: String,

    @field:NotBlank(message = "Пароль не может быть пустым")
    val password: String
)

