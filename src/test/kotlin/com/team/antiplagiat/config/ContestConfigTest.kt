package com.team.antiplagiat.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

class ContestConfigTest {

    @Test
    fun `maxDurationHours is bound from application properties`() {
        val source = MapConfigurationPropertySource(
            mapOf("app.contest.max-duration-hours" to "12")
        )

        val contestConfig = Binder(source)
            .bind("app.contest", Bindable.of(ContestConfig::class.java))
            .get()

        assertEquals(12, contestConfig.maxDurationHours)
    }
}

