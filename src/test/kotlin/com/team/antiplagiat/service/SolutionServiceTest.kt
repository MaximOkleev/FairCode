//package com.team.antiplagiat.service
//
//import com.team.antiplagiat.models.Solution
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import java.time.LocalDateTime
//
//class SolutionServiceTest {
//
//    private lateinit var service: SolutionService
//
//    @BeforeEach
//    fun setUp() {
//        val properties = AntiPlagiatProperties(maxAttempts = 2)
//        service = SolutionService(properties)
//    }
//
//    @AfterEach
//    fun tearDown() {
//        service.entities.clear()
//    }
//
//    private fun createSolution(id: Long, userId: Long, taskId: Long): Solution {
//        return Solution(
//            id = id,
//            userId = userId,
//            taskId = taskId,
//            language = "kotlin",
//            filePath = "/test/$id",
//            date = LocalDateTime.now()
//        )
//    }
//
//    @Test
//    fun `проверяет, что созданная сущность сохраняется в entities`() {
//        val solution = createSolution(1, 10, 100)
//
//        service.create(solution)
//
//        assertTrue(service.entities.containsKey(1))
//        assertEquals(solution, service.entities[1])
//    }
//
//    @Test
//    fun `проверяет лимит попыток создания (не больше maxAttempts)` () {
//        val solution1 = createSolution(1, 10, 100)
//        val solution2 = createSolution(2, 10, 100)
//        val solution3 = createSolution(3, 10, 100)
//
//        assertTrue(service.create(solution1))
//        assertTrue(service.create(solution2))
//        assertFalse(service.create(solution3))
//    }
//
//    @Test
//    fun `проверяет успешное обновление существующей сущности` () {
//        val solution = createSolution(1, 10, 100)
//        service.create(solution)
//
//        val updated = createSolution(1, 20, 200)
//
//        val result = service.update(1, updated)
//
//        assertTrue(result)
//        assertEquals(updated, service.entities[1])
//    }
//
//    @Test
//    fun `проверяет, что нельзя обновить несуществующую сущность` () {
//        val updated = createSolution(1, 20, 200)
//
//        val result = service.update(1, updated)
//
//        assertFalse(result)
//    }
//    @Test
//    fun `проверяет, что нельзя создать две сущности с одинаковым id`() {
//        val solution1 = createSolution(1, 10, 100)
//        val solution2 = createSolution(1, 20, 200)
//
//        assertTrue(service.create(solution1))
//        assertFalse(service.create(solution2))
//    }
//
//    @Test
//    fun `проверяет, что лимит попыток считается отдельно для разных пользователей`() {
//        val s1 = createSolution(1, 10, 100)
//        val s2 = createSolution(2, 10, 100)
//        val s3 = createSolution(3, 20, 100)
//        val s4 = createSolution(4, 20, 100)
//
//        assertTrue(service.create(s1))
//        assertTrue(service.create(s2))
//        assertFalse(service.create(createSolution(5, 10, 100)))
//
//        assertTrue(service.create(s3))
//        assertTrue(service.create(s4))
//    }
//
//    @Test
//    fun `проверяет, что лимит попыток считается отдельно для разных задач одного пользователя`() {
//        val s1 = createSolution(1, 10, 100)
//        val s2 = createSolution(2, 10, 100)
//        val s3 = createSolution(3, 10, 200)
//        val s4 = createSolution(4, 10, 200)
//
//        assertTrue(service.create(s1))
//        assertTrue(service.create(s2))
//        assertFalse(service.create(createSolution(5, 10, 100)))
//
//        assertTrue(service.create(s3))
//        assertTrue(service.create(s4))
//    }
//
//    @Test
//    fun `проверяет, что update полностью заменяет старое значение`() {
//        val original = createSolution(1, 10, 100)
//        service.create(original)
//
//        val updated = createSolution(1, 999, 999)
//        service.update(1, updated)
//
//        assertNotEquals(original, service.entities[1])
//        assertEquals(updated, service.entities[1])
//    }
//
//    @Test
//    fun `проверяет, что при неудачном create счетчик попыток не увеличивается`() {
//        val s1 = createSolution(1, 10, 100)
//        val s2 = createSolution(1, 10, 100)
//
//        assertTrue(service.create(s1))
//        assertFalse(service.create(s2))
//
//        val s3 = createSolution(2, 10, 100)
//        assertTrue(service.create(s3))
//    }
//}


