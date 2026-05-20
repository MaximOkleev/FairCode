package com.team.antiplagiat.service

import com.team.antiplagiat.config.ResendProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

private val resendLogger = KotlinLogging.logger {}

@Service
class ResendService(
    restTemplateBuilder: RestTemplateBuilder,
    private val properties: ResendProperties
) {

    private val restTemplate = restTemplateBuilder.build()

    fun send(
        to: String,
        subject: String,
        html: String
    ) {
        if (!properties.enabled) {
            resendLogger.info { "Resend delivery is disabled" }
            return
        }
        require(properties.apiKey.isNotBlank()) { "RESEND_API_KEY is required when Resend delivery is enabled" }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(properties.apiKey)
        }

        val body = mapOf(
            "from" to properties.from,
            "to" to listOf(to),
            "subject" to subject,
            "html" to html
        )

        val request = HttpEntity(body, headers)
        restTemplate.postForEntity(
            "https://api.resend.com/emails",
            request,
            String::class.java
        )
    }
}
