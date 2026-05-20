package com.team.antiplagiat.service

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.ProblemRepository
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import io.mockk.Runs
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional


@ExtendWith(MockKExtension::class)
class ProblemServiceTest {

    @MockK
    private lateinit var problemRepository: ProblemRepository

    @MockK(relaxed = true)
    private lateinit var meterRegistry: MeterRegistry

    @InjectMockKs
    private lateinit var problemService: ProblemService

    private lateinit var problem: Problem

    @BeforeEach
    fun setUp() {
        problem = Problem(
            id = 1L,
            name = "Sum of numbers",
            description = "Find the sum of two numbers"
        )
    }

    @Test
    fun `problem fields are set correctly on creation`() {
        val problem = Problem(id = 1L, name = "Test", description = "Description")

        assertEquals(1L, problem.id)
        assertEquals("Test", problem.name)
        assertEquals("Description", problem.description)
    }

    @Test
    fun `problem has null description by default`() {
        val problem = Problem(id = 0L, name = "")

        assertNull(problem.description)
        assertEquals(0L, problem.id)
    }

    @Test
    fun `create saves and returns problem with description`() {
        every { problemRepository.save(any()) } returns problem

        val result = problemService.create("Sum of numbers", "Find the sum of two numbers")

        assertEquals("Sum of numbers", result.name)
        assertEquals("Find the sum of two numbers", result.description)
        verify(exactly = 1) {
            problemRepository.save(match {
                it.name == "Sum of numbers" &&
                    it.description == "Find the sum of two numbers"
            })
        }
    }

    @Test
    fun `create saves problem with null description`() {
        val problemWithoutDescription = Problem(id = 2L, name = "Factorial", description = null)
        every { problemRepository.save(any()) } returns problemWithoutDescription

        val result = problemService.create("Factorial", null)

        assertEquals("Factorial", result.name)
        assertNull(result.description)
        verify(exactly = 1) {
            problemRepository.save(match {
                it.name == "Factorial" && it.description == null
            })
        }
    }

    @Test
    fun `findById returns problem for existing id`() {
        every { problemRepository.findById(1L) } returns Optional.of(problem)

        val result = problemService.findById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Sum of numbers", result?.name)
    }

    @Test
    fun `findById returns null for missing id`() {
        every { problemRepository.findById(99L) } returns Optional.empty()

        val result = problemService.findById(99L)

        assertNull(result)
    }

    @Test
    fun `findAll returns all problems`() {
        val problems = listOf(
            problem,
            Problem(id = 2L, name = "Factorial", description = null),
            Problem(id = 3L, name = "Palindrome", description = "Check the string")
        )
        every { problemRepository.findAll() } returns problems

        val result = problemService.findAll()

        assertEquals(3, result.size)
        assertEquals("Sum of numbers", result[0].name)
        assertEquals("Factorial", result[1].name)
    }

    @Test
    fun `findAll returns empty list when repository is empty`() {
        every { problemRepository.findAll() } returns emptyList()

        val result = problemService.findAll()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `update changes existing problem and returns saved value`() {
        val updated = Problem(id = 1L, name = "New title", description = "New description")
        every { problemRepository.findById(1L) } returns Optional.of(problem)
        every { problemRepository.save(any()) } returns updated

        val result = problemService.update(1L, "New title", "New description")

        assertNotNull(result)
        assertEquals("New title", result?.name)
        assertEquals("New description", result?.description)
    }

    @Test
    fun `update changes only name when description is null`() {
        every { problemRepository.findById(1L) } returns Optional.of(problem)
        every { problemRepository.save(any()) } answers { firstArg() }

        val result = problemService.update(1L, "New title", null)

        assertNotNull(result)
        assertEquals("New title", result?.name)
        assertEquals("Find the sum of two numbers", result?.description)
    }

    @Test
    fun `update returns null and does not save for missing problem`() {
        every { problemRepository.findById(99L) } returns Optional.empty()

        val result = problemService.update(99L, "Title", "Description")

        assertNull(result)
        verify(exactly = 0) { problemRepository.save(any()) }
    }

    @Test
    fun `update changes only description when name is null`() {
        every { problemRepository.findById(1L) } returns Optional.of(problem)
        every { problemRepository.save(any()) } answers { firstArg() }

        val result = problemService.update(1L, null, "New description")

        assertNotNull(result)
        assertEquals("Sum of numbers", result?.name)
        assertEquals("New description", result?.description)
    }

    @Test
    fun `update keeps problem unchanged when both fields are null`() {
        every { problemRepository.findById(1L) } returns Optional.of(problem)
        every { problemRepository.save(any()) } answers { firstArg() }

        val result = problemService.update(1L, null, null)

        assertNotNull(result)
        assertEquals("Sum of numbers", result?.name)
        assertEquals("Find the sum of two numbers", result?.description)
    }

     @Test
     fun `delete throws ResourceNotFoundException for missing id`() {
         every { problemRepository.existsById(99L) } returns false

         assertThrows(ResourceNotFoundException::class.java) { problemService.delete(99L) }
         verify(exactly = 0) { problemRepository.deleteById(any()) }
     }

      @Test
      fun `delete calls repository for existing id`() {
          every { problemRepository.existsById(1L) } returns true
          every { problemRepository.deleteById(1L) } just Runs

          problemService.delete(1L)

          verify(exactly = 1) { problemRepository.deleteById(1L) }
      }
}
