package com.team.antiplagiat.config

import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException

class DatabaseUserDetailsServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userDetailsService: DatabaseUserDetailsService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userDetailsService = DatabaseUserDetailsService(userRepository)
    }

    @Test
    fun `loadUserByUsername returns user details when user exists`() {
        val user = User(
            id = 1L,
            login = "testuser",
            email = "test@example.com",
            role = User.Role.BASIC,
            passwordHash = "\$2a\$10\$hashedpassword"
        )

        every { userRepository.findByLogin("testuser") } returns user

        val userDetails = userDetailsService.loadUserByUsername("testuser")

        assertNotNull(userDetails)
        assertEquals("testuser", userDetails.username)
        assertEquals("\$2a\$10\$hashedpassword", userDetails.password)
        assertTrue(userDetails.isAccountNonExpired)
        assertTrue(userDetails.isAccountNonLocked)
        assertTrue(userDetails.isCredentialsNonExpired)
        assertTrue(userDetails.isEnabled)
    }

    @Test
    fun `loadUserByUsername returns BASIC role authority`() {
        val user = User(
            id = 1L,
            login = "basicuser",
            email = "basic@example.com",
            role = User.Role.BASIC,
            passwordHash = "password1"
        )

        every { userRepository.findByLogin("basicuser") } returns user

        val userDetails = userDetailsService.loadUserByUsername("basicuser")

        assertEquals(1, userDetails.authorities.size)
        val authority = userDetails.authorities.first()
        assertEquals("ROLE_BASIC", authority.authority)
        assertTrue(authority is SimpleGrantedAuthority)
    }

    @Test
    fun `loadUserByUsername returns ADMIN role authority`() {
        val user = User(
            id = 2L,
            login = "adminuser",
            email = "admin@example.com",
            role = User.Role.ADMIN,
            passwordHash = "password2"
        )

        every { userRepository.findByLogin("adminuser") } returns user

        val userDetails = userDetailsService.loadUserByUsername("adminuser")

        assertEquals(1, userDetails.authorities.size)
        val authority = userDetails.authorities.first()
        assertEquals("ROLE_ADMIN", authority.authority)
    }

    @Test
    fun `loadUserByUsername throws UsernameNotFoundException when user not found`() {
        every { userRepository.findByLogin("nonexistent") } returns null

        val exception = assertThrows(UsernameNotFoundException::class.java) {
            userDetailsService.loadUserByUsername("nonexistent")
        }

        assertEquals("User not found: nonexistent", exception.message)
    }

    @Test
    fun `loadUserByUsername throws UsernameNotFoundException with correct message format`() {
        val username = "notfound"
        every { userRepository.findByLogin(username) } returns null

        val exception = assertThrows(UsernameNotFoundException::class.java) {
            userDetailsService.loadUserByUsername(username)
        }

        assertTrue(exception.message?.contains(username) ?: false)
        assertTrue(exception.message?.contains("User not found") ?: false)
    }

    @Test
    fun `loadUserByUsername preserves user login as username`() {
        val user = User(
            id = 1L,
            login = "mylogin",
            email = "user@example.com",
            role = User.Role.BASIC,
            passwordHash = "hash"
        )

        every { userRepository.findByLogin("mylogin") } returns user

        val userDetails = userDetailsService.loadUserByUsername("mylogin")

        assertEquals("mylogin", userDetails.username)
    }

    @Test
    fun `loadUserByUsername preserves password hash`() {
        val passwordHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcg7b3XeKeUxWdeS86E36DRjk38"
        val user = User(
            id = 1L,
            login = "user1",
            email = "user1@example.com",
            role = User.Role.BASIC,
            passwordHash = passwordHash
        )

        every { userRepository.findByLogin("user1") } returns user

        val userDetails = userDetailsService.loadUserByUsername("user1")

        assertEquals(passwordHash, userDetails.password)
    }

    @Test
    fun `loadUserByUsername sets all account flags to enabled`() {
        val user = User(
            id = 1L,
            login = "activeuser",
            email = "active@example.com",
            role = User.Role.BASIC,
            passwordHash = "hash"
        )

        every { userRepository.findByLogin("activeuser") } returns user

        val userDetails = userDetailsService.loadUserByUsername("activeuser")

        assertTrue(userDetails.isAccountNonExpired, "Account should not be expired")
        assertTrue(userDetails.isAccountNonLocked, "Account should not be locked")
        assertTrue(userDetails.isCredentialsNonExpired, "Credentials should not be expired")
        assertTrue(userDetails.isEnabled, "User should be enabled")
    }

    @Test
    fun `loadUserByUsername calls repository findByLogin once`() {
        val user = User(
            id = 1L,
            login = "user",
            email = "user@example.com",
            role = User.Role.BASIC,
            passwordHash = "hash"
        )

        every { userRepository.findByLogin("user") } returns user

        userDetailsService.loadUserByUsername("user")

        io.mockk.verify(exactly = 1) { userRepository.findByLogin("user") }
    }

    @Test
    fun `loadUserByUsername handles special characters in username`() {
        val user = User(
            id = 1L,
            login = "user-123_test@domain",
            email = "user@example.com",
            role = User.Role.BASIC,
            passwordHash = "hash"
        )

        every { userRepository.findByLogin("user-123_test@domain") } returns user

        val userDetails = userDetailsService.loadUserByUsername("user-123_test@domain")

        assertEquals("user-123_test@domain", userDetails.username)
    }

    @Test
    fun `loadUserByUsername case sensitivity`() {
        val user = User(
            id = 1L,
            login = "TestUser",
            email = "test@example.com",
            role = User.Role.BASIC,
            passwordHash = "hash"
        )

        every { userRepository.findByLogin("TestUser") } returns user

        val userDetails = userDetailsService.loadUserByUsername("TestUser")

        assertEquals("TestUser", userDetails.username)
    }

    @Test
    fun `loadUserByUsername with empty password hash`() {
        val user = User(
            id = 1L,
            login = "user",
            email = "user@example.com",
            role = User.Role.BASIC,
            passwordHash = ""
        )

        every { userRepository.findByLogin("user") } returns user

        val userDetails = userDetailsService.loadUserByUsername("user")

        assertEquals("", userDetails.password)
        assertNotNull(userDetails.authorities)
    }
}

