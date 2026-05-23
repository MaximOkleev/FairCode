package com.team.antiplagiat.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("AntiPlagiat API")
                    .description("API для проверки плагиата решений задач\n\n" +
                        "**Основные фичи:**\n" +
                        "- Загрузка решений код-документов\n" +
                        "- Проверка на плагиат\n" +
                        "- Управление пользователями и задачами\n" +
                        "- Подробные отчеты о совпадениях\n\n" +
                        "**Безопасность:**\n" +
                        "- Spring Security (JWT Bearer authentication)\n" +
                        "- Swagger Authorize button for Bearer token\n" +
                        "- Role-based access (ADMIN, BASIC)\n" +
                        "- BCrypt password hashing\n\n" +
                        "**Версионирование:**\n" +
                        "- REST API v1\n" +
                        "- PostgreSQL database\n" +
                        "- Docker поддержка")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("AntiPlagiat Team")
                            .email("team@antiplagiat.com")
                            .url("https://github.com/antiplagiat")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .addServersItem(
                Server()
                    .url("/")
                    .description("Current server")
            )
            .components(
                Components().addSecuritySchemes(
                    "bearerAuth",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .`in`(SecurityScheme.In.HEADER)
                )
            )
            .addSecurityItem(
                SecurityRequirement().addList("bearerAuth")
            )
    }
}
