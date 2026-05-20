package com.team.antiplagiat.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProblemTest {

    @Test
    fun `problem has expected default values`() {
        val problem = Problem()

        assertEquals(0L, problem.id)
        assertEquals("", problem.name)
        assertNull(problem.description)
    }

    @Test
    fun `problem keeps custom values`() {
        val problem = Problem(
            id = 5L,
            name = "Two Sum",
            description = "Find two numbers that add up to target"
        )

        assertEquals(5L, problem.id)
        assertEquals("Two Sum", problem.name)
        assertEquals("Find two numbers that add up to target", problem.description)
    }

    @Test
    fun `problem description can be null`() {
        val p1 = Problem(id = 1, name = "Problem 1", description = null)
        val p2 = Problem(id = 2, name = "Problem 2", description = "Has description")

        assertNull(p1.description)
        assertEquals("Has description", p2.description)
    }

    @Test
    fun `problem fields are mutable`() {
        val problem = Problem(id = 1, name = "Original", description = "Old")

        problem.name = "Updated"
        problem.description = "New"

        assertEquals("Updated", problem.name)
        assertEquals("New", problem.description)
    }

    @Test
    fun `multiple problems have independent data`() {
        val problems = listOf(
            Problem(id = 1, name = "P1", description = "D1"),
            Problem(id = 2, name = "P2", description = null),
            Problem(id = 3, name = "P3", description = "D3")
        )

        assertEquals(3, problems.size)
        assertEquals("P1", problems[0].name)
        assertNull(problems[1].description)
        assertEquals("D3", problems[2].description)
    }
}

