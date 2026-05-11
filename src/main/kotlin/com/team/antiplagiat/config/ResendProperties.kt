package com.team.antiplagiat.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import kotlin.properties.Delegates

@Configuration
@ConfigurationProperties(prefix = "app.resend")
class ResendProperties {
    var apiKey by Delegates.notNull<String>()
    var from by Delegates.notNull<String>()
    var baseUrl by Delegates.notNull<String>()
}