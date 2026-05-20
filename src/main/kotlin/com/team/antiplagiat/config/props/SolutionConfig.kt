package com.team.antiplagiat.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.properties.Delegates

@Configuration
@ConfigurationProperties(prefix = "app.solution")
class SolutionConfig {
    var maxAttempts by Delegates.notNull<Int>()
}
