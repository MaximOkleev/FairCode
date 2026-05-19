package com.team.antiplagiat.handler

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.InvalidCredentialsException
import com.team.antiplagiat.exception.TooManyAttemptsException
import com.team.antiplagiat.filter.TraceIdFilter
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(
        ex: ResourceNotFoundException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)
        logger.warn { "ResourceNotFoundException | traceId=$traceId |message=${ex.message}" }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Ресурс не найден",
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(TooManyAttemptsException::class)
    fun handleTooManyAttempts(
        ex: TooManyAttemptsException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)

        logger.warn {
            "TooManyAttemptsException | traceId=$traceId | message=${ex.message}"
        }

        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Превышен лимит попыток",
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolation(
        ex: DataIntegrityViolationException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)
        val message = ex.rootCause?.message ?: ex.message ?: "Конфликт данных"

        logger.warn {
            "DataIntegrityViolationException | traceId=$traceId | message=$message"
        }

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    message = message,
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)
        val violations = ex.constraintViolations
            .joinToString("; ") { "${it.propertyPath}: ${it.message}" }

        logger.warn {
            "ConstraintViolationException | traceId=$traceId | violations=$violations"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = violations.ifBlank { "Ошибка валидации ограничений" },
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)

        logger.warn {
            "IllegalArgumentException | traceId=$traceId | message=${ex.message}"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Неверный аргумент",
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(
        ex: InvalidCredentialsException
    ): ResponseEntity<ErrorResponse> {
        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)

        logger.warn {
            "InvalidCredentialsException | traceId=$traceId"
        }

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Invalid email or password",
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)

        val errors = ex.bindingResult.allErrors
            .joinToString(", ") {
                it.defaultMessage ?: "Неверное поле"
            }

        logger.warn {
            "Validation failed | traceId=$traceId | errors=$errors"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = errors,
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameter(
        ex: MissingServletRequestParameterException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)

        logger.warn {
            "Missing parameter: ${ex.parameterName} | traceId=$traceId"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = "Отсутствует параметр: ${ex.parameterName}",
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(
        ex: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)

        logger.warn {
            "HttpMessageNotReadable | traceId=$traceId"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    message = "Неверный формат запроса",
                    traceId = traceId
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {

        val traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY)


        logger.error(ex) {
            "Необработанное исключение | traceId=$traceId | ${ex.javaClass.simpleName}: ${ex.message}"
        }

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    message = ex.message ?: "Внутренняя ошибка сервера",
                    traceId = traceId
                )
            )
    }
}

data class ErrorResponse(
    val message: String,
    val traceId: String?,
    val timestamp: String = Instant.now().toString()
)