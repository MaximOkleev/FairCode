package com.team.antiplagiat.config

import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.properties.Delegates

@ConfigurationProperties(prefix = "app.contest")
class ContestConfig {
    var maxDurationHours by Delegates.notNull<Int>()
}