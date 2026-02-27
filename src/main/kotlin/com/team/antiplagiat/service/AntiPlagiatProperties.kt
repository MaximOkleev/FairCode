package com.team.antiplagiat.service

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "antiplagiat")
class AntiPlagiatProperties {
    var maxAttempts: Int = 50

    @PostConstruct
    fun log() {
        println(">>> AntiPlagiatProperties loaded: maxAttemptsPerTask = $maxAttempts")
    }
}