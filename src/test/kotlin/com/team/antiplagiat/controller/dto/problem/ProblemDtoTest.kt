package com.team.antiplagiat.controller.dto.problem

import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.controller.dto.ProblemRequest as RootProblemRequest
import com.team.antiplagiat.controller.dto.ProblemResponse as RootProblemResponse
import com.team.antiplagiat.controller.dto.toEntity as toRootEntity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ProblemDtoTest {

    @Test
    fun `ProblemRequest toEntity should create Problem correctly`() {
        val request = ProblemRequest(
            name = "Two Sum",
            description = "Find two numbers that add up to target"
        )

        val problem = request.toEntity()

        assertEquals("Two Sum", problem.name)
        assertEquals("Find two numbers that add up to target", problem.description)
        assertEquals(0L, problem.id)
    }

    @Test
    fun `ProblemRequest toEntity should handle null description`() {
        val request = ProblemRequest(
            name = "Factorial",
            description = null
        )

        val problem = request.toEntity()

        assertEquals("Factorial", problem.name)
        assertNull(problem.description)
    }

    @Test
    fun `ProblemResponse fromEntity should convert Problem correctly`() {
        val problem = Problem(
            id = 1L,
            name = "Fibonacci",
            description = "Calculate fibonacci number"
        )

        val response = ProblemResponse.fromEntity(problem)

        assertEquals(1L, response.id)
        assertEquals("Fibonacci", response.name)
        assertEquals("Calculate fibonacci number", response.description)
    }

    @Test
    fun `ProblemResponse should handle null description from entity`() {
        val problem = Problem(
            id = 2L,
            name = "Prime Check",
            description = null
        )

        val response = ProblemResponse.fromEntity(problem)

        assertEquals(2L, response.id)
        assertEquals("Prime Check", response.name)
        assertNull(response.description)
    }

    @Test
    fun `ProblemRequest and Response should be reversible`() {
        val originalProblem = Problem(
            id = 5L,
            name = "Linked List",
            description = "Operations on linked lists"
        )

        val response = ProblemResponse.fromEntity(originalProblem)
        val request = ProblemRequest(
            name = response.name,
            description = response.description
        )
        val reconstructed = request.toEntity()

        assertEquals(originalProblem.name, reconstructed.name)
        assertEquals(originalProblem.description, reconstructed.description)
    }

    @Test
    fun `root ProblemRequest toEntity should create Problem correctly`() {
        val request = RootProblemRequest(name = "Root Problem", description = "Root Description")

        val entity = request.toRootEntity()

        assertEquals("Root Problem", entity.name)
        assertEquals("Root Description", entity.description)
        assertEquals(0L, entity.id)
    }

    @Test
    fun `root ProblemResponse fromEntity should convert Problem correctly`() {
        val problem = Problem(id = 8L, name = "Root Fibonacci", description = null)

        val response = RootProblemResponse.fromEntity(problem)

        assertEquals(8L, response.id)
        assertEquals("Root Fibonacci", response.name)
        assertNull(response.description)
    }
}

