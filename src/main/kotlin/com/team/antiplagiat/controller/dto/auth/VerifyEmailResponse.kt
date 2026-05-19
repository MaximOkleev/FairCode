package com.team.antiplagiat.controller.dto.auth

data class VerifyEmailResponse(
    val message: String,
    val success: Boolean,
    val userId: Long? = null,
    val token: String? = null
)

