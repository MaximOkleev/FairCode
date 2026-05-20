package com.team.antiplagiat.handler

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.TooManyAttemptsException
import com.team.antiplagiat.exception.RateLimitException
import com.team.antiplagiat.exception.TokenExpiredException
import com.team.antiplagiat.exception.TokenAlreadyUsedException
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleNotFound returns 404 with message`() {
        val exception = ResourceNotFoundException("Пользователь не найден")

        val response = handler.handleNotFound(exception)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Пользователь не найден", response.body?.message)
    }

    @Test
    fun `global handler handleNotFound returns default message when null`() {
        val exception = ResourceNotFoundException("")

        val response = handler.handleNotFound(exception)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

     @Test
     fun `handleTooManyAttempts returns 429 with message`() {
         val exception = TooManyAttemptsException("Превышен лимит: 5 попыток")

         val response = handler.handleTooManyAttempts(exception)

         assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
         assertNotNull(response.body)
         assertEquals("Превышен лимит: 5 попыток", response.body?.message)
     }

     @Test
     fun `handleRateLimit returns 429 with message`() {
         val exception = RateLimitException("Too many requests. Try again later")

         val response = handler.handleRateLimit(exception)

         assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.statusCode)
         assertNotNull(response.body)
         assertEquals("Too many requests. Try again later", response.body?.message)
     }

     @Test
     fun `handleTokenExpired returns 400 with message`() {
         val exception = TokenExpiredException("Token expired")

         val response = handler.handleTokenExpired(exception)

         assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
         assertNotNull(response.body)
         assertEquals("Token expired", response.body?.message)
     }

     @Test
     fun `handleTokenAlreadyUsed returns 400 with message`() {
         val exception = TokenAlreadyUsedException("Token already used")

         val response = handler.handleTokenAlreadyUsed(exception)

         assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
         assertNotNull(response.body)
         assertEquals("Token already used", response.body?.message)
     }

    @Test
    fun `handleMissingParameter returns 400 with parameter name`() {
        val paramException = mockk<MissingServletRequestParameterException>()
        io.mockk.every { paramException.parameterName } returns "status"

        val response = handler.handleMissingParameter(paramException)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Отсутствует параметр: status", response.body?.message)
    }

    @Test
    fun `handleIllegalArgument returns 400`() {
        val exception = IllegalArgumentException("Неверный аргумент: value")

        val response = handler.handleIllegalArgument(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Неверный аргумент: value", response.body?.message)
    }

    @Test
    fun `handleNotReadable returns 400 for invalid format`() {
        val exception = mockk<HttpMessageNotReadableException>()

        val response = handler.handleNotReadable(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Неверный формат запроса", response.body?.message)
    }

    @Test
    fun `handleGeneric returns 500 for unknown exception`() {
        val exception = Exception("Unknown error")

        val response = handler.handleGeneric(exception)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Unknown error", response.body?.message)
    }

    @Test
    fun `handleValidation returns 400 with validation errors`() {
        val bindingResult = mockk<org.springframework.validation.BindingResult>()
        val error1 = mockk<org.springframework.validation.ObjectError>()
        val error2 = mockk<org.springframework.validation.ObjectError>()

        io.mockk.every { error1.defaultMessage } returns "Field is required"
        io.mockk.every { error2.defaultMessage } returns "Invalid format"
        io.mockk.every { bindingResult.allErrors } returns listOf(error1, error2)

        val exception = mockk<MethodArgumentNotValidException>()
        io.mockk.every { exception.bindingResult } returns bindingResult

        val response = handler.handleValidation(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        assertEquals("Field is required, Invalid format", response.body?.message)
    }

    @Test
    fun `ErrorResponse data class works correctly`() {
        val errorResponse = ErrorResponse("Test error message", traceId = null)

        assertEquals("Test error message", errorResponse.message)
    }
}

