package com.team.antiplagiat.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "antiplagiat")
class AntiPlagiatProperties(
    var maxAttempts: Int = 50
)
