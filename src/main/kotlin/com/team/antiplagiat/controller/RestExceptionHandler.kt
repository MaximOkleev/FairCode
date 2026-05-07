@file:Suppress("UNUSED")

package com.team.antiplagiat.controller

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import com.team.antiplagiat.exception.ResourceNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Instant

private val logger = KotlinLogging.logger {}

@ControllerAdvice
@Suppress("UNUSED")
class RestExceptionHandler {

    @Suppress("UNUSED")
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, req: HttpServletRequest): ResponseEntity<Any> {
        val msg = ex.rootCause?.message ?: ex.message ?: "Conflict"
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = msg,
            path = req.requestURI
        )
        logger.warn { "Data integrity violation for request=${req.method} ${req.requestURI}: $msg" }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException, req: HttpServletRequest): ResponseEntity<Any> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Validation failed",
            path = req.requestURI,
            errors = errors
        )
        logger.warn { "Validation failed for request=${req.method} ${req.requestURI}: $errors" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @Suppress("UNUSED")
    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleConstraintViolation(ex: ConstraintViolationException, req: HttpServletRequest): ResponseEntity<Any> {
        val errors = ex.constraintViolations.associate { it.propertyPath.toString() to (it.message ?: "invalid") }
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = "Constraint violations",
            path = req.requestURI,
            errors = errors
        )
        logger.warn { "Constraint violations for request=${req.method} ${req.requestURI}: $errors" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }

    @Suppress("UNUSED")
    @ExceptionHandler(Exception::class)
    fun handleAll(ex: Exception, req: HttpServletRequest): ResponseEntity<Any> {
        logger.error(ex) { "Unhandled exception for request=${req.method} ${req.requestURI}" }
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = (ex.message ?: "An unexpected error occurred"),
            path = req.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }

    @Suppress("UNUSED")
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException, req: HttpServletRequest): ResponseEntity<Any> {
        val body = ApiError(
            timestamp = Instant.now().toString(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = (ex.message ?: "Resource not found"),
            path = req.requestURI
        )
        logger.warn { "Resource not found for request=${req.method} ${req.requestURI}: ${ex.message}" }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }
}

