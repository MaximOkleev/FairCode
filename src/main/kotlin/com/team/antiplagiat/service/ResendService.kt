package com.team.antiplagiat.service

import com.team.antiplagiat.config.ResendProperties
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class ResendService(
    private val restTemplateBuilder: RestTemplateBuilder,
    private val properties: ResendProperties
) {

    private val restTemplate = restTemplateBuilder.build()

    fun send(
        to: String,
        subject: String,
        html: String
    ) {
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