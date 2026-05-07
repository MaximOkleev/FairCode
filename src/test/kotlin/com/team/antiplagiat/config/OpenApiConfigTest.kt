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
        assertTrue(api.servers.isNotEmpty())
    }
}

