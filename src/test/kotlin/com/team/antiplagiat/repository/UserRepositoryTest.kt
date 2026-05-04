package com.team.antiplagiat.repository

import com.team.antiplagiat.models.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `findByLogin  returns user when exist`() {
        val suffix = System.nanoTime().toString().takeLast(6)
        val user = User(
            login = "testuser-$suffix",
            email = "test-$suffix@example.com",
            role = User.Role.BASIC
        )
        entityManager.persistAndFlush(user)

        val found = userRepository.findByLogin("testuser-$suffix")

        assertNotNull(found)
        assertEquals("testuser-$suffix", found?.login)
    }

    @Test
    fun `findByLogin returns null when user not found`() {
        val found = userRepository.findByLogin("nonexistent-${System.nanoTime()}")
        assertNull(found)
    }

    @Test
    fun `Save and findById preserve user data`() {
        val suffix = System.nanoTime().toString().takeLast(6)
        val user = User(
            login = "newuser-$suffix",
            email = "new-$suffix@example.com",
            role = User.Role.ADMIN,
            passwordHash = "\$2a\$10\$test"
        )

        val saved = userRepository.saveAndFlush(user)
        entityManager.clear()
        val loaded = userRepository.findById(saved.id).orElse(null)

        assertNotNull(loaded)
        assertEquals("newuser-$suffix", loaded?.login)
        assertEquals(User.Role.ADMIN, loaded?.role)
    }

    @Test
    fun `findAll return users`() {
        val suffix = System.nanoTime().toString().takeLast(6)
        val user1 = User(login = "user1-$suffix", email = "user1-$suffix@example.com", role = User.Role.BASIC)
        val user2 = User(login = "user2-$suffix", email = "user2-$suffix@example.com", role = User.Role.ADMIN)

        entityManager.persistAndFlush(user1)
        entityManager.persistAndFlush(user2)
        entityManager.flush()

        val all = userRepository.findAll()
        val createdUsers = all.filter { it.login.contains(suffix) }
        assertEquals(2, createdUsers.size)
    }

    @Test
    fun `deleteById removes user`() {
        val suffix = System.nanoTime().toString().takeLast(6)
        val user = User(login = "todelete-$suffix", email = "delete-$suffix@example.com", role = User.Role.BASIC)
        val saved = userRepository.saveAndFlush(user)
        userRepository.deleteById(saved.id)
        entityManager.flush()
        entityManager.clear()
        val loaded = userRepository.findById(saved.id)
        assertEquals(false, loaded.isPresent)
    }

    @Test
    fun `upd user data`() {
        val suffix = System.nanoTime().toString().takeLast(6)
        val user = User(login = "original-$suffix", email = "original-$suffix@example.com", role = User.Role.BASIC)
        val saved = userRepository.saveAndFlush(user)

        saved.login = "updated-$suffix"
        saved.role = User.Role.ADMIN
        userRepository.saveAndFlush(saved)
        entityManager.clear()
        val loaded = userRepository.findById(saved.id).orElse(null)
        assertEquals("updated-$suffix", loaded?.login)
        assertEquals(User.Role.ADMIN, loaded?.role)
    }

    @Test
    fun `users with different roles`() {
        val suffix = System.nanoTime().toString().takeLast(6)
        val basicUser = User(login = "basic-$suffix", email = "basic-$suffix@example.com", role = User.Role.BASIC)
        val adminUser = User(login = "admin-$suffix", email = "admin-$suffix@example.com", role = User.Role.ADMIN)
        entityManager.persistAndFlush(basicUser)
        entityManager.persistAndFlush(adminUser)
        entityManager.flush()

        val all = userRepository.findAll()
        val createdUsers = all.filter { it.login.contains(suffix) }
        assertEquals(2, createdUsers.size)
        assertEquals(1, createdUsers.count { it.role == User.Role.BASIC })
        assertEquals(1, createdUsers.count { it.role == User.Role.ADMIN })
    }
}

