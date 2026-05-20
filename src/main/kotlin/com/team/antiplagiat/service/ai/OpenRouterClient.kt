package com.team.antiplagiat.service.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.team.antiplagiat.config.props.AiGenerationProperties
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

@Component
class OpenRouterClient(
    private val objectMapper: ObjectMapper,
    private val properties: AiGenerationProperties
) {
    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build()

    fun generate(modelName: String, prompt: String): OpenRouterGenerationResult {
        val config = properties.openrouter
        require(config.apiKey.isNotBlank()) { "OPENROUTER_API_KEY is not configured" }

        val body = linkedMapOf<String, Any?>(
            "model" to modelName,
            "messages" to listOf(
                mapOf("role" to "system", "content" to SYSTEM_PROMPT),
                mapOf("role" to "user", "content" to prompt)
            ),
            "temperature" to properties.temperature
        )
        if (properties.maxTokens > 0) {
            body["max_completion_tokens"] = properties.maxTokens
        }

        val requestBuilder = HttpRequest.newBuilder(URI.create("${config.baseUrl.trimEnd('/')}/chat/completions"))
            .timeout(Duration.ofSeconds(120))
            .header("Authorization", "Bearer ${config.apiKey}")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))

        if (config.httpReferer.isNotBlank()) {
            requestBuilder.header("HTTP-Referer", config.httpReferer)
        }
        if (config.appTitle.isNotBlank()) {
            requestBuilder.header("X-OpenRouter-Title", config.appTitle)
        }

        val response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            throw IllegalStateException("OpenRouter returned HTTP ${response.statusCode()}: ${response.body().take(500)}")
        }

        val root = objectMapper.readTree(response.body())
        val content = root.path("choices").path(0).path("message").path("content").asText("")
            .trim()
            .ifBlank { throw IllegalStateException("OpenRouter response does not contain generated text") }
        val actualModel = root.path("model").asText(modelName)

        return OpenRouterGenerationResult(actualModel, stripMarkdownFences(content))
    }

    private fun stripMarkdownFences(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.startsWith("```")) {
            return trimmed
        }

        val lines = trimmed.lines().drop(1).toMutableList()
        if (lines.lastOrNull()?.trim() == "```") {
            lines.removeAt(lines.lastIndex)
        }
        return lines.joinToString("\n").trim()
    }

    private companion object {
        const val SYSTEM_PROMPT = "Generate only the final source code. Do not wrap it in Markdown."
    }
}
