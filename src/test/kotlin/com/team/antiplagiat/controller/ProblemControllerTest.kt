package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.ProblemRequest
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.service.ProblemService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
class ProblemControllerTest {

    @MockK
    private lateinit var problemService: ProblemService

    @InjectMockKs
    private lateinit var problemController: ProblemController

    @Test
    fun `create returns created response with body`() {
        val request = ProblemRequest(
            name = "Two Sum",
            description = "Find indices of two numbers"
        )
        val savedProblem = Problem(
            id = 1L,
            name = "Two Sum",
            description = "Find indices of two numbers"
        )
        every { problemService.create("Two Sum", "Find indices of two numbers") } returns savedProblem

        val response = problemController.create(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertEquals(1L, response.body?.id)
        assertEquals("Two Sum", response.body?.name)
        assertEquals("Find indices of two numbers", response.body?.description)
        verify(exactly = 1) {
            problemService.create("Two Sum", "Find indices of two numbers")
        }
    }

    @Test
    fun `get returns ok response for existing problem`() {
        val problem = Problem(
            id = 1L,
            name = "Two Sum",
            description = "Find indices of two numbers"
        )
        every { problemService.findById(1L) } returns problem

        val response = problemController.get(1L)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(1L, response.body?.id)
        assertEquals("Two Sum", response.body?.name)
        assertEquals("Find indices of two numbers", response.body?.description)
        verify(exactly = 1) { problemService.findById(1L) }
    }

    @Test
    fun `get returns not found for missing problem`() {
        every { problemService.findById(99L) } returns null

        val response = problemController.get(99L)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
        verify(exactly = 1) { problemService.findById(99L) }
    }

    @Test
    fun `getAll returns mapped response list`() {
        every { problemService.findAll() } returns listOf(
            Problem(id = 1L, name = "Two Sum", description = "Find indices"),
            Problem(id = 2L, name = "Factorial", description = null)
        )

        val response = problemController.getAll()

        assertEquals(2, response.size)
        assertEquals(1L, response[0].id)
        assertEquals("Two Sum", response[0].name)
        assertEquals("Find indices", response[0].description)
        assertEquals(2L, response[1].id)
        assertEquals("Factorial", response[1].name)
        assertNull(response[1].description)
        verify(exactly = 1) { problemService.findAll() }
    }

    @Test
    fun `update returns ok response for existing problem`() {
        val request = ProblemRequest(
            name = "Updated title",
            description = "Updated description"
        )
        val updatedProblem = Problem(
            id = 1L,
            name = "Updated title",
            description = "Updated description"
        )
        every {
            problemService.update(1L, "Updated title", "Updated description")
        } returns updatedProblem

        val response = problemController.update(1L, request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals(1L, response.body?.id)
        assertEquals("Updated title", response.body?.name)
        assertEquals("Updated description", response.body?.description)
        verify(exactly = 1) {
            problemService.update(1L, "Updated title", "Updated description")
        }
    }

    @Test
    fun `update returns not found for missing problem`() {
        val request = ProblemRequest(
            name = "Updated title",
            description = "Updated description"
        )
        every {
            problemService.update(99L, "Updated title", "Updated description")
        } returns null

        val response = problemController.update(99L, request)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNull(response.body)
        verify(exactly = 1) {
            problemService.update(99L, "Updated title", "Updated description")
        }
    }

    @Test
    fun `delete returns no content and calls service`() {
        every { problemService.delete(1L) } just Runs

        val response = problemController.delete(1L)

        assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        assertNull(response.body)
        verify(exactly = 1) { problemService.delete(1L) }
    }
}
