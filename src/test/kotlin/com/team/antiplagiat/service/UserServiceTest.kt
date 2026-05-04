package com.team.antiplagiat.service

import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `create should save and return user`() {
        val user = User(id = 0L, login = "testuser", email = "test@example.com", role = User.Role.BASIC)
        val savedUser = User(id = 1L, login = "testuser", email = "test@example.com", role = User.Role.BASIC)

        every { userRepository.save(user) } returns savedUser

        val result = userService.create(user)

        assertEquals(1L, result.id)
        assertEquals("testuser", result.login)
        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `findById returns user when exists`() {
        val userId = 1L
        val user = User(id = userId, login = "testuser", email = "test@example.com", role = User.Role.BASIC)

        every { userRepository.findById(userId) } returns Optional.of(user)

        val result = userService.findById(userId)

        assertNotNull(result)
        assertEquals(userId, result?.id)
        assertEquals("testuser", result?.login)
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `findById returns null when user not found`() {
        val userId = 999L

        every { userRepository.findById(userId) } returns Optional.empty()

        val result = userService.findById(userId)

        assertNull(result)
        verify(exactly = 1) { userRepository.findById(userId) }
    }

    @Test
    fun `findAll returns list of users`() {
        val users = listOf(
            User(id = 1L, login = "user1", email = "user1@example.com", role = User.Role.BASIC),
            User(id = 2L, login = "user2", email = "user2@example.com", role = User.Role.ADMIN)
        )

        every { userRepository.findAll() } returns users

        val result = userService.findAll()

        assertEquals(2, result.size)
        assertEquals("user1", result[0].login)
        assertEquals(User.Role.ADMIN, result[1].role)
        verify(exactly = 1) { userRepository.findAll() }
    }

    @Test
    fun `findAll returns empty list when no users`() {
        every { userRepository.findAll() } returns emptyList()

        val result = userService.findAll()

        assertTrue(result.isEmpty())
        verify(exactly = 1) { userRepository.findAll() }
    }

    @Test
    fun `update returns updated user when found`() {
        val userId = 1L
        val existingUser = User(id = userId, login = "old", email = "old@example.com", role = User.Role.BASIC)
        val updatedUser = User(id = userId, login = "updated", email = "updated@example.com", role = User.Role.BASIC)

        every { userRepository.findById(userId) } returns Optional.of(existingUser)
        every { userRepository.save(existingUser) } returns updatedUser

        val result = userService.update(userId, "updated", "updated@example.com")

        assertNotNull(result)
        assertEquals("updated", result?.login)
        assertEquals("updated@example.com", result?.email)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 1) { userRepository.save(existingUser) }
    }

    @Test
    fun `update with only login change`() {
        val userId = 1L
        val existingUser = User(id = userId, login = "old", email = "test@example.com", role = User.Role.BASIC)
        val updatedUser = User(id = userId, login = "newlogin", email = "test@example.com", role = User.Role.BASIC)

        every { userRepository.findById(userId) } returns Optional.of(existingUser)
        every { userRepository.save(existingUser) } returns updatedUser

        val result = userService.update(userId, "newlogin", null)

        assertNotNull(result)
        assertEquals("newlogin", result?.login)
        assertEquals("test@example.com", result?.email)
    }

    @Test
    fun `update with only email change`() {
        val userId = 1L
        val existingUser = User(id = userId, login = "user", email = "old@example.com", role = User.Role.BASIC)
        val updatedUser = User(id = userId, login = "user", email = "new@example.com", role = User.Role.BASIC)

        every { userRepository.findById(userId) } returns Optional.of(existingUser)
        every { userRepository.save(existingUser) } returns updatedUser

        val result = userService.update(userId, null, "new@example.com")

        assertNotNull(result)
        assertEquals("user", result?.login)
        assertEquals("new@example.com", result?.email)
    }

    @Test
    fun `update returns null when user not found`() {
        val userId = 999L

        every { userRepository.findById(userId) } returns Optional.empty()

        val result = userService.update(userId, "newlogin", "newemail@example.com")

        assertNull(result)
        verify(exactly = 1) { userRepository.findById(userId) }
        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `delete calls repository deleteById`() {
        val userId = 1L

        every { userRepository.deleteById(userId) } just runs

        userService.delete(userId)

        verify(exactly = 1) { userRepository.deleteById(userId) }
    }

    @Test
    fun `update with both null values does not update`() {
        val userId = 1L
        val existingUser = User(id = userId, login = "user", email = "test@example.com", role = User.Role.BASIC)
        val sameUser = User(id = userId, login = "user", email = "test@example.com", role = User.Role.BASIC)

        every { userRepository.findById(userId) } returns Optional.of(existingUser)
        every { userRepository.save(existingUser) } returns sameUser

        val result = userService.update(userId, null, null)

        assertNotNull(result)
        assertEquals("user", result?.login)
        assertEquals("test@example.com", result?.email)
    }
}

