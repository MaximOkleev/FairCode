package com.team.antiplagiat.controller.dto.auth

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AuthDtoTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `forgot password request rejects blank email`() {
        val req = ForgotPasswordRequest(email = "")

        val violations = validator.validate(req)

        assertEquals(1, violations.size)
        assertTrue(violations.any { it.message.contains("Email не может быть пустым") })
    }

    @Test
    fun `forgot password request accepts valid email`() {
        val req = ForgotPasswordRequest(email = "user@example.com")

        val violations = validator.validate(req)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `forgot password request behaves like a data class`() {
        val req = ForgotPasswordRequest(email = "user@example.com")
        val (email) = req

        assertEquals("user@example.com", req.email)
        assertEquals("user@example.com", email)
        assertEquals(req, req.copy())
        assertEquals(req.hashCode(), req.copy().hashCode())
        assertEquals("ForgotPasswordRequest(email=user@example.com)", req.toString())
    }

    @Test
    fun `reset password request rejects blank token and password`() {
        val req = ResetPasswordRequest(token = "", newPassword = "")

        val violations = validator.validate(req)
        val messages = violations.map { it.message }

        assertEquals(2, violations.size)
        assertTrue(messages.any { it.contains("Token не может быть пустым") })
        assertTrue(messages.any { it.contains("Новый пароль не может быть пустым") })
    }

    @Test
    fun `reset password request accepts valid payload`() {
        val req = ResetPasswordRequest(token = "token-123", newPassword = "strong-password")

        val violations = validator.validate(req)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `reset password request behaves like a data class`() {
        val req = ResetPasswordRequest(token = "token-123", newPassword = "strong-password")
        val (token, newPassword) = req

        assertEquals("token-123", req.token)
        assertEquals("strong-password", req.newPassword)
        assertEquals("token-123", token)
        assertEquals("strong-password", newPassword)
        assertEquals(req, req.copy())
        assertEquals(req.hashCode(), req.copy().hashCode())
        assertEquals("ResetPasswordRequest(token=token-123, newPassword=strong-password)", req.toString())
    }


    @Test
    fun `change password request rejects blank old and new password`() {
        val req = ChangePasswordRequest(oldPassword = "", newPassword = "")

        val violations = validator.validate(req)
        val messages = violations.map { it.message }

        assertEquals(2, violations.size)
        assertTrue(messages.any { it.contains("Старый пароль не может быть пустым") })
        assertTrue(messages.any { it.contains("Новый пароль не может быть пустым") })
    }

    @Test
    fun `change password request accepts valid payload`() {
        val req = ChangePasswordRequest(oldPassword = "old-pass", newPassword = "new-pass")

        val violations = validator.validate(req)

        assertTrue(violations.isEmpty())
    }

    @Test
    fun `change password request behaves like a data class`() {
        val req = ChangePasswordRequest(oldPassword = "old-pass", newPassword = "new-pass")
        val (oldPassword, newPassword) = req

        assertEquals("old-pass", req.oldPassword)
        assertEquals("new-pass", req.newPassword)
        assertEquals("old-pass", oldPassword)
        assertEquals("new-pass", newPassword)
        assertEquals(req, req.copy())
        assertEquals(req.hashCode(), req.copy().hashCode())
        assertEquals("ChangePasswordRequest(oldPassword=old-pass, newPassword=new-pass)", req.toString())
    }

    @Test
    fun `login response keeps token and optional message`() {
        val response = LoginResponse(token = "jwt-token", message = "ok")

        assertEquals("jwt-token", response.token)
        assertEquals("ok", response.message)
    }

    @Test
    fun `login response behaves like a data class`() {
        val response = LoginResponse(token = "jwt-token", message = "ok")
        val (token, message) = response

        assertEquals("jwt-token", token)
        assertEquals("ok", message)
        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertEquals("LoginResponse(token=jwt-token, message=ok)", response.toString())
    }

    @Test
    fun `login response defaults message to null`() {
        val response = LoginResponse(token = "jwt-token")

        assertEquals("jwt-token", response.token)
        assertNull(response.message)
    }

    @Test
    fun `verify email response keeps all fields`() {
        val response = VerifyEmailResponse(
            message = "Email verified",
            success = true,
            userId = 42L,
            token = "verification-token"
        )

        assertEquals("Email verified", response.message)
        assertTrue(response.success)
        assertEquals(42L, response.userId)
        assertEquals("verification-token", response.token)
    }

    @Test
    fun `verify email response behaves like a data class`() {
        val response = VerifyEmailResponse(
            message = "Email verified",
            success = true,
            userId = 42L,
            token = "verification-token"
        )
        val (message, success, userId, token) = response

        assertEquals("Email verified", message)
        assertTrue(success)
        assertEquals(42L, userId)
        assertEquals("verification-token", token)
        assertEquals(response, response.copy())
        assertEquals(response.hashCode(), response.copy().hashCode())
        assertEquals(
            "VerifyEmailResponse(message=Email verified, success=true, userId=42, token=verification-token)",
            response.toString()
        )
    }

    @Test
    fun `verify email response defaults optional fields to null`() {
        val response = VerifyEmailResponse(message = "Email verified", success = true)

        assertEquals("Email verified", response.message)
        assertTrue(response.success)
        assertNull(response.userId)
        assertNull(response.token)
    }
}

