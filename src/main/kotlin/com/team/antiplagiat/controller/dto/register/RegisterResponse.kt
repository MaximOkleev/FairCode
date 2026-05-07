package com.team.antiplagiat.controller.dto.register

data class RegisterResponse(
    val userId: Long,
    val email: String,
    val token: String
)