package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.Problem
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProblemDTOTest {

    @Test
    fun `fromEntity maps problem fields to response`() {
        val problem = mockk<Problem>()
        every { problem.id } returns 10L
        every { problem.name } returns "Palindrome"
        every { problem.description } returns "Check whether a string is a palindrome"

        val response = ProblemResponse.fromEntity(problem)

        assertEquals(10L, response.id)
        assertEquals("Palindrome", response.name)
        assertEquals("Check whether a string is a palindrome", response.description)
    }

    @Test
    fun `fromEntity keeps null description in response`() {
        val problem = mockk<Problem>()
        every { problem.id } returns 11L
        every { problem.name } returns "Factorial"
        every { problem.description } returns null

        val response = ProblemResponse.fromEntity(problem)

        assertEquals(11L, response.id)
        assertEquals("Factorial", response.name)
        assertNull(response.description)
    }

    @Test
    fun `toEntity maps request fields to problem`() {
        val request = ProblemRequest(
            name = "Two Sum",
            description = "Find indices of two numbers"
        )

        val problem = request.toEntity()

        assertEquals(0L, problem.id)
        assertEquals("Two Sum", problem.name)
        assertEquals("Find indices of two numbers", problem.description)
    }

    @Test
    fun `toEntity keeps null description`() {
        val request = ProblemRequest(
            name = "Factorial",
            description = null
        )

        val problem = request.toEntity()

        assertEquals(0L, problem.id)
        assertEquals("Factorial", problem.name)
        assertNull(problem.description)
    }
}

