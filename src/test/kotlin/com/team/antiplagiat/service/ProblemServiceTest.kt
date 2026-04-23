package com.team.antiplagiat.service

import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.ProblemRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ProblemServiceTest {

    @Mock
    private lateinit var problemRepository: ProblemRepository

    @InjectMocks
    private lateinit var problemService: ProblemService

    private lateinit var problem: Problem

    @BeforeEach
    fun setUp() {
        problem = Problem(id = 1L, name = "Сумма чисел", description = "Найти сумму двух чисел")
    }

    // -------------------------
    // CREATE
    // -------------------------

    @Test
    fun `when Problem created - then fields are set correctly`() {
        val problem = Problem(id = 1L, name = "Тест", description = "Описание")

        assertEquals(1L, problem.id)
        assertEquals("Тест", problem.name)
        assertEquals("Описание", problem.description)
    }

    @Test
    fun `when Problem created with default values - then description is null`() {
        val problem = Problem(id = 0L, name = "")

        assertNull(problem.description)
        assertEquals(0L, problem.id)
    }

    @Test
    fun `when create called with name and description - then saves and returns problem`() {
        `when`(problemRepository.save(any(Problem::class.java))).thenReturn(problem)

        val result = problemService.create("Сумма чисел", "Найти сумму двух чисел")

        assertEquals("Сумма чисел", result.name)
        assertEquals("Найти сумму двух чисел", result.description)
        verify(problemRepository, times(1)).save(any(Problem::class.java))
    }

    @Test
    fun `when create called with null description - then saves problem without description`() {
        val problemNoDesc = Problem(id = 2L, name = "Факториал", description = null)
        `when`(problemRepository.save(any(Problem::class.java))).thenReturn(problemNoDesc)

        val result = problemService.create("Факториал", null)

        assertEquals("Факториал", result.name)
        assertNull(result.description)
    }

    // -------------------------
    // FIND BY ID
    // -------------------------

    @Test
    fun `when findById called with existing id - then returns problem`() {
        `when`(problemRepository.findById(1L)).thenReturn(Optional.of(problem))

        val result = problemService.findById(1L)

        assertNotNull(result)
        assertEquals(1L, result?.id)
        assertEquals("Сумма чисел", result?.name)
    }

    @Test
    fun `when findById called with non-existing id - then returns null`() {
        `when`(problemRepository.findById(99L)).thenReturn(Optional.empty())

        val result = problemService.findById(99L)

        assertNull(result)
    }

    // -------------------------
    // FIND ALL
    // -------------------------

    @Test
    fun `when findAll called - then returns all problems`() {
        val problems = listOf(
            problem,
            Problem(id = 2L, name = "Факториал", description = null),
            Problem(id = 3L, name = "Палиндром", description = "Проверить строку")
        )
        `when`(problemRepository.findAll()).thenReturn(problems)

        val result = problemService.findAll()

        assertEquals(3, result.size)
        assertEquals("Сумма чисел", result[0].name)
        assertEquals("Факториал", result[1].name)
    }

    @Test
    fun `when findAll called and no problems exist - then returns empty list`() {
        `when`(problemRepository.findAll()).thenReturn(emptyList())

        val result = problemService.findAll()

        assertTrue(result.isEmpty())
    }

    // -------------------------
    // UPDATE
    // -------------------------

    @Test
    fun `when update called with existing id - then updates and returns problem`() {
        val updated = Problem(id = 1L, name = "Новое название", description = "Новое описание")
        `when`(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        `when`(problemRepository.save(any(Problem::class.java))).thenReturn(updated)

        val result = problemService.update(1L, "Новое название", "Новое описание")

        assertNotNull(result)
        assertEquals("Новое название", result?.name)
        assertEquals("Новое описание", result?.description)
    }

    @Test
    fun `when update called with null description - then updates only name`() {
        `when`(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        `when`(problemRepository.save(any(Problem::class.java))).thenAnswer { it.arguments[0] }

        val result = problemService.update(1L, "Новое название", null)

        assertNotNull(result)
        assertEquals("Новое название", result?.name)
        assertEquals("Найти сумму двух чисел", result?.description)
    }

    @Test
    fun `when update called with non-existing id - then returns null without saving`() {
        `when`(problemRepository.findById(99L)).thenReturn(Optional.empty())

        val result = problemService.update(99L, "Название", "Описание")

        assertNull(result)
        verify(problemRepository, never()).save(any(Problem::class.java))
    }

    @Test
    fun `when update called with null name - then updates only description`() {
        `when`(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        `when`(problemRepository.save(any(Problem::class.java))).thenAnswer { it.arguments[0] }

        val result = problemService.update(1L, null, "Новое описание")

        assertNotNull(result)
        assertEquals("Сумма чисел", result?.name)
        assertEquals("Новое описание", result?.description)
    }

    @Test
    fun `when update called with both null - then returns problem unchanged`() {
        `when`(problemRepository.findById(1L)).thenReturn(Optional.of(problem))
        `when`(problemRepository.save(any(Problem::class.java))).thenAnswer { it.arguments[0] }

        val result = problemService.update(1L, null, null)

        assertNotNull(result)
        assertEquals("Сумма чисел", result?.name)
        assertEquals("Найти сумму двух чисел", result?.description)
    }

    // -------------------------
    // DELETE
    // -------------------------

    @Test
    fun `when delete called with existing id - then calls deleteById`() {
        doNothing().`when`(problemRepository).deleteById(1L)

        problemService.delete(1L)

        verify(problemRepository, times(1)).deleteById(1L)
    }

    @Test
    fun `when delete called with non-existing id - then does not throw exception`() {
        doNothing().`when`(problemRepository).deleteById(99L)

        assertDoesNotThrow { problemService.delete(99L) }
        verify(problemRepository, times(1)).deleteById(99L)
    }
}