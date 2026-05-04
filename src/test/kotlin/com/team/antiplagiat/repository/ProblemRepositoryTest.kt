package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Problem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class ProblemRepositoryTest {

    @Autowired
    private lateinit var problemRepository: ProblemRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `Save and findById preserve problem`() {
        val problem = Problem(name = "Two Sum", description = "Find two numbers")
        val saved = problemRepository.saveAndFlush(problem)
        entityManager.clear()
        val loaded = problemRepository.findById(saved.id).orElse(null)

        assertNotNull(loaded)
        assertEquals("Two Sum", loaded?.name)
    }

    @Test
    fun `findAll return all problems`() {
        problemRepository.saveAndFlush(Problem(name = "P1", description = "D1"))
        problemRepository.saveAndFlush(Problem(name = "P2", description = null))
        val all = problemRepository.findAll()
        assertEquals(2, all.size)
    }

    @Test
    fun `deleteById remove problem`() {
        val saved = problemRepository.saveAndFlush(Problem(name = "Delete", description = "Test"))
        problemRepository.deleteById(saved.id)
        entityManager.flush()
        entityManager.clear()
        assertEquals(false, problemRepository.findById(saved.id).isPresent)
    }

    @Test
    fun `Update problem changes data`() {
        val saved = problemRepository.saveAndFlush(Problem(name = "Old", description = "Old"))
        saved.name = "New"
        problemRepository.saveAndFlush(saved)
        entityManager.clear()
        val loaded = problemRepository.findById(saved.id).orElse(null)
        assertEquals("New", loaded?.name)
    }
}

