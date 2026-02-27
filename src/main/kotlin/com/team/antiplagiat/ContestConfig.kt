package com.team.antiplagiat

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.contest")
class ContestConfig {
    var maxDurationHours: Long = 5

    @PostConstruct
    fun log() {
        println(">>> ContestConfig loaded: maxDurationHours = $maxDurationHours")
    }
}