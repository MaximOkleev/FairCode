package com.team.antiplagiat.config

import java.time.Instant

data class SecurityErrorResponse(
    val message: String,
    val traceId: String?,
    val timestamp: String = Instant.now().toString()
)

