package com.team.antiplagiat.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.ai-generation")
class AiGenerationProperties {
    var openrouter: OpenRouter = OpenRouter()
    var models: Models = Models()
    var temperature: Double = 0.2
    var maxTokens: Int = 4096

    class OpenRouter {
        var apiKey: String = ""
        var baseUrl: String = "https://openrouter.ai/api/v1"
        var httpReferer: String = ""
        var appTitle: String = "antiplagiat"
    }

    class Models {
        var chatgpt: String = "openai/gpt-5.5"
        var gemini: String = "google/gemini-3.1-flash-lite"
        var deepseek: String = "deepseek/deepseek-v4-flash"
    }
}
