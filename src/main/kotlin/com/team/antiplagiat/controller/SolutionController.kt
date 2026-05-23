package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.solution.SolutionResponse
import com.team.antiplagiat.controller.dto.solution.SolutionTextRequest
import com.team.antiplagiat.service.SolutionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/solutions")
@Tag(name = "Solutions", description = "API для управления решениями код-документов")
class SolutionController(private val solutionService: SolutionService) {

    @GetMapping("/{id}")
    @Operation(
        summary = "Получить решение по ID",
        description = "Возвращает полную информацию о решении с указанным ID"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Решение найдено"),
            ApiResponse(responseCode = "401", description = "Не авторизирован"),
            ApiResponse(responseCode = "403", description = "Запрещено"),
            ApiResponse(responseCode = "404", description = "Решение не найдено"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        ]
    )
    fun get(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SolutionResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/solutions/$id - пользователь ${payload.userId}" }
        val solution = solutionService.findById(id)

        // Пользователь может получить только свое решение или администратор может получить любое
        if (solution.user.id != payload.userId && payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался получить решение ${id} другого пользователя" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        logger.debug { "Решение $id получено" }
        return ResponseEntity.ok(SolutionResponse.fromEntity(solution))
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Изменить решение по ID",
        description = "Обновляет язык, путь файла и текст кода у решения текущего пользователя."
    )
    fun update(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long,
        @Valid @RequestBody request: SolutionTextRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SolutionResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val updated = solutionService.updateOwned(
            ownerId = payload.userId,
            solutionId = id,
            language = request.language,
            filePath = request.filePath,
            code = request.code
        )
        return ResponseEntity.ok(SolutionResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Удалить решение",
        description = "Удаляет решение и все связанные данные из системы"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Решение успешно удалено"),
            ApiResponse(responseCode = "401", description = "Не авторизирован"),
            ApiResponse(responseCode = "403", description = "Запрещено"),
            ApiResponse(responseCode = "404", description = "Решение не найдено")
        ]
    )
    fun delete(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "DELETE /api/solutions/$id - пользователь ${payload.userId}" }
        val solution = solutionService.findById(id)

        // Пользователь может удалить только свое решение или администратор может удалить любое
        if (solution.user.id != payload.userId && payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался удалить решение ${id} другого пользователя" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        solutionService.delete(id)
        logger.info { "Решение $id удалено пользователем ${payload.userId}" }
        return ResponseEntity.noContent().build()
    }
}
