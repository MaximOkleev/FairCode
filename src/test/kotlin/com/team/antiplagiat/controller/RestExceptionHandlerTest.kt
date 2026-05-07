package com.team.antiplagiat.controller

import com.team.antiplagiat.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

class RestExceptionHandlerTest {

    private val handler = RestExceptionHandler()
    private val mockRequest = mockk<HttpServletRequest>()

    init {
        every { mockRequest.requestURI } returns "/api/test"
        every { mockRequest.method } returns "POST"
    }

    @Test
    fun `handleValidation returns 400 with field errors`() {
        val bindingResult = mockk<org.springframework.validation.BindingResult>()
        val fieldError = mockk<FieldError>()

        every { fieldError.field } returns "email"
        every { fieldError.defaultMessage } returns "must be a valid email"
        every { bindingResult.fieldErrors } returns listOf(fieldError)

        val exception = mockk<MethodArgumentNotValidException>()
        every { exception.bindingResult } returns bindingResult

        val response = handler.handleValidation(exception, mockRequest)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        val body = response.body as ApiError
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.status)
        assertEquals("Validation failed", body.message)
        assertEquals("/api/test", body.path)
    }

    @Test
    fun `handleConstraintViolation returns 400`() {
        val violation = mockk<ConstraintViolation<*>>()
        val path = mockk<jakarta.validation.Path>()

        every { path.toString() } returns "userId"
        every { violation.propertyPath } returns path
        every { violation.message } returns "must not be null"

        val exception = ConstraintViolationException("Constraint violation", setOf(violation))

        val response = handler.handleConstraintViolation(exception, mockRequest)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        val body = response.body as ApiError
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.status)
        assertEquals("Constraint violations", body.message)
    }

    @Test
    fun `handleAll returns 500 for generic exception`() {
        val exception = Exception("Something went wrong")

        val response = handler.handleAll(exception, mockRequest)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        val body = response.body as ApiError
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.status)
        assertEquals("Internal Server Error", body.error)
        assertEquals("Something went wrong", body.message)
    }

    @Test
    fun `handleAll returns default message when null`() {
        val exception = Exception()

        val response = handler.handleAll(exception, mockRequest)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertNotNull(response.body)
        val body = response.body as ApiError
        assertEquals("An unexpected error occurred", body.message)
    }

    @Test
    fun `handleNotFound returns 404 for ResourceNotFoundException`() {
        val exception = ResourceNotFoundException("User not found")

        val response = handler.handleNotFound(exception, mockRequest)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
        val body = response.body as ApiError
        assertEquals(HttpStatus.NOT_FOUND.value(), body.status)
        assertEquals("Not Found", body.error)
        assertEquals("User not found", body.message)
        assertEquals("/api/test", body.path)
    }

    @Test
    fun `handleNotFound returns default message when null`() {
        val exception = ResourceNotFoundException("")

        val response = handler.handleNotFound(exception, mockRequest)

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertNotNull(response.body)
    }

    @Test
    fun `response includes timestamp for all handlers`() {
        val exception = Exception("Test")

        val response = handler.handleAll(exception, mockRequest)

        assertNotNull(response.body)
        val body = response.body as ApiError
        assertNotNull(body.timestamp)
    }

    @Test
    fun `validation error response includes errors map`() {
        val bindingResult = mockk<org.springframework.validation.BindingResult>()
        val fieldError = mockk<FieldError>()

        every { fieldError.field } returns "password"
        every { fieldError.defaultMessage } returns "must be at least 8 characters"
        every { bindingResult.fieldErrors } returns listOf(fieldError)

        val exception = mockk<MethodArgumentNotValidException>()
        every { exception.bindingResult } returns bindingResult

        val response = handler.handleValidation(exception, mockRequest)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertNotNull(response.body)
        val body = response.body as ApiError
        val errors = body.errors
        assertNotNull(errors)
        assertEquals("must be at least 8 characters", errors!!["password"])
    }
}

