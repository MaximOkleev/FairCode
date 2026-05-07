package com.team.antiplagiat.controller

import java.time.Instant

data class ApiError(
    val timestamp: String = Instant.now().toString(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val errors: Map<String, String>? = null
)

