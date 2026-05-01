package com.team.antiplagiat.handler

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.TooManyAttemptsException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Ресурс не найден"))
    }

    @ExceptionHandler(TooManyAttemptsException::class)
    fun handleTooManyAttempts(ex: TooManyAttemptsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse(ex.message ?: "Превышен лимит попыток"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("Внутренняя ошибка сервера"))
    }
}

data class ErrorResponse(val message: String)