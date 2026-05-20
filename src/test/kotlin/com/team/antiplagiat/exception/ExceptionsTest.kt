package com.team.antiplagiat.exception

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class ResourceNotFoundExceptionTest {
    @Test
    fun `ResourceNotFoundException can be created with message`() {
        val exception = ResourceNotFoundException("User not found")
        assertEquals("User not found", exception.message)
    }
}

class InvalidCredentialsExceptionTest {
    @Test
    fun `InvalidCredentialsException can be created with message`() {
        val exception = InvalidCredentialsException("Invalid password")
        assertEquals("Invalid password", exception.message)
    }
}

class TokenExpiredExceptionTest {
    @Test
    fun `TokenExpiredException can be created with message`() {
        val exception = TokenExpiredException("Token has expired")
        assertEquals("Token has expired", exception.message)
    }
}

class TokenAlreadyUsedExceptionTest {
    @Test
    fun `TokenAlreadyUsedException can be created with message`() {
        val exception = TokenAlreadyUsedException("Token was already used")
        assertEquals("Token was already used", exception.message)
    }
}

class TooManyAttemptsExceptionTest {
    @Test
    fun `TooManyAttemptsException can be created with message`() {
        val exception = TooManyAttemptsException("Too many login attempts")
        assertEquals("Too many login attempts", exception.message)
    }
}

class RateLimitExceptionTest {
    @Test
    fun `RateLimitException can be created with message`() {
        val exception = RateLimitException("Rate limit exceeded")
        assertEquals("Rate limit exceeded", exception.message)
    }
}

class ImportFailedExceptionTest {
    @Test
    fun `ImportFailedException can be created with message`() {
        val exception = ImportFailedException("Import failed")
        assertEquals("Import failed", exception.message)
    }
}

