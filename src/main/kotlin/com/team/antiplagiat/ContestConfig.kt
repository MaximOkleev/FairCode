package com.team.antiplagiat

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app.contest")
class ContestConfig {
    var maxDurationHours: Long = 5


}