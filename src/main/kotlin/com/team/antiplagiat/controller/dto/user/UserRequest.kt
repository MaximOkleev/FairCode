package com.team.antiplagiat.controller.dto.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserRequest(
    @field:NotBlank(message = "Логин не может быть пустым")
    @field:Size(min = 3, max = 50, message = "Логин должен содержать от 3 до 50 символов")
    val login: String,

    @field:Email(message = "Некорректный формат email")
    @field:NotBlank(message = "Email не может быть пустым")
    val email: String
)