package com.team.antiplagiat.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Instant

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        val body = mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to "Validation failed",
            "path" to req.requestURI,
            "errors" to errors
        )
        logger.warn { "Validation failed for request=${req.method} ${req.requestURI}: $errors" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolation(ex: ConstraintViolationException, req: HttpServletRequest): ResponseEntity<Any> {
        val errors = ex.constraintViolations.associate { it.propertyPath.toString() to (it.message ?: "invalid") }
        val body = mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to HttpStatus.BAD_REQUEST.value(),
            "error" to "Bad Request",
            "message" to "Constraint violations",
            "path" to req.requestURI,
            "errors" to errors
        )
        logger.warn { "Constraint violations for request=${req.method} ${req.requestURI}: $errors" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception, req: HttpServletRequest): ResponseEntity<Any> {
        logger.error(ex) { "Unhandled exception for request=${req.method} ${req.requestURI}" }
        val body = mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error" to "Internal Server Error",
            "message" to (ex.message ?: "An unexpected error occurred"),
            "path" to req.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}

