package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank(message = "Старый пароль не может быть пустым")
    val oldPassword: String,

    @field:NotBlank(message = "Новый пароль не может быть пустым")
    val newPassword: String
)
