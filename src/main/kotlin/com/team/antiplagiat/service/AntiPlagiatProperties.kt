package com.team.antiplagiat.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "antiplagiat")
class AntiPlagiatProperties {
    val maxAttempts: Int = 0
}