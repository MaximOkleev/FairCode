package com.team.antiplagiat.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.properties.Delegates

@Configuration
@ConfigurationProperties(prefix = "app.contest")
class ContestConfig {
    var maxDurationHours by Delegates.notNull<Int>()
}