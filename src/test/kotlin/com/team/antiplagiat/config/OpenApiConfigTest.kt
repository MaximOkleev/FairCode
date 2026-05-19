package com.team.antiplagiat.config

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class OpenApiConfigTest {

    @Test
    fun `customOpenAPI returns populated OpenAPI`() {
        val cfg = OpenApiConfig()
        val api = cfg.customOpenAPI()
        assertNotNull(api.info)
        assertTrue(api.info.title.contains("AntiPlagiat"))
        assertTrue(api.info.description.contains("JWT Bearer authentication"))
        assertTrue(api.info.description.contains("Swagger Authorize button for Bearer token"))
        assertTrue(api.servers.isNotEmpty())
        assertNotNull(api.components)
        assertTrue(api.components.securitySchemes.containsKey("bearerAuth"))
        assertTrue(api.security?.any { it.containsKey("bearerAuth") } == true)
    }
}

