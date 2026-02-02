package com.team.antiplagiat.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.contest")
data class ContestConfig(
    val maxDurationHours: Long = 5
)