package com.team.antiplagiat.controller.dto.auth

data class LoginResponse(
    val token: String,
    val message: String? = null
)
