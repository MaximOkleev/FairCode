package com.team.antiplagiat.controller.dto.register

data class RegisterResponse(
    val userId: Long,
    val login: String,
    val email: String,
    val message: String = "Пользователь зарегистрирован. Проверьте почту для подтверждения email",
    val emailVerificationRequired: Boolean = true
)