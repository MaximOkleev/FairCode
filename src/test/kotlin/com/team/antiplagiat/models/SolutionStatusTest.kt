package com.team.antiplagiat.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SolutionStatusTest {

    @Test
    fun `fromString converts valid statuses`() {
        assertEquals(SolutionStatus.WAITING, SolutionStatus.fromString("waiting"))
        assertEquals(SolutionStatus.COMPLETED, SolutionStatus.fromString("completed"))
    }

    @Test
    fun `fromString is case insensitive`() {
        assertEquals(SolutionStatus.PROCESSING, SolutionStatus.fromString("processing"))
        assertEquals(SolutionStatus.PROCESSING, SolutionStatus.fromString("PROCESSING"))
    }

    @Test
    fun `fromString throws for invalid status`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SolutionStatus.fromString("invalid")
        }
        assertEquals("Неизвестный статус: invalid", exception.message)
    }
}

