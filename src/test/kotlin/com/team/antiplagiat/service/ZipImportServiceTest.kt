package com.team.antiplagiat.service

import com.team.antiplagiat.config.props.ZipImportProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.util.unit.DataSize

class ZipImportServiceTest {

    @Test
    fun `zip import properties have safe defaults`() {
        val props = ZipImportProperties()

        assertEquals(1000, props.maxFiles)
        assertEquals(DataSize.ofMegabytes(5), props.maxEntrySize)
    }
}
