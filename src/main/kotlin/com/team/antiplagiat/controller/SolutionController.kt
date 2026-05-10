package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.solution.SolutionRequest
import com.team.antiplagiat.controller.dto.solution.SolutionResponse
import com.team.antiplagiat.models.SolutionStatus
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

    @PostMapping
    @Operation(
        summary = "Создать новое решение",
        description = "Загружает новое решение для проверки на плагиат. Требует problemId, language и filePath. userId извлекается из токена."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Решение успешно создано"),
            ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            ApiResponse(responseCode = "401", description = "Не авторизирован"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        ]
    )
    fun create(
        @Valid @RequestBody request: SolutionRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SolutionResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "POST /api/solutions - создание от пользователя ${payload.userId}, задача ${request.problemId}" }
        if (request.problemId <= 0) {
            logger.warn { "Пользователь ${payload.userId} отправил неверный problemId: ${request.problemId}" }
            return ResponseEntity.badRequest().build()
        }
        val solution = solutionService.create(
            userId = payload.userId,
            problemId = request.problemId,
            language = request.language,
            filePath = request.filePath,
            code = request.code
        )
        logger.info { "Решение создано: id=${solution.id}, пользователь ${payload.userId}" }
        return ResponseEntity.status(HttpStatus.CREATED).body(SolutionResponse.fromEntity(solution))
    }

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
        if (solution == null) {
            return ResponseEntity.notFound().build()
        }

        // Пользователь может получить только свое решение или администратор может получить любое
        if (solution.user.id != payload.userId && payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался получить решение ${id} другого пользователя" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        logger.debug { "Решение $id получено" }
        return ResponseEntity.ok(SolutionResponse.fromEntity(solution))
    }

    @GetMapping
    @Operation(
        summary = "Получить все решения",
        description = "Возвращает список решений. Администраторы видят все решения, обычные пользователи видят только свои."
    )
    @ApiResponse(responseCode = "200", description = "Список решений успешно получен")
    fun getAll(httpRequest: HttpServletRequest): ResponseEntity<List<SolutionResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/solutions - пользователь ${payload.userId}" }
        val solutions = if (payload.role == "ADMIN") {
            logger.debug { "Администратор ${payload.userId} получает все решения" }
            solutionService.findAll()
        } else {
            logger.debug { "Пользователь ${payload.userId} получает свои решения" }
            solutionService.findByUser(payload.userId)
        }

        logger.debug { "Найдено решений: ${solutions.size}" }
        return ResponseEntity.ok(solutions.map { SolutionResponse.fromEntity(it) })
    }

    @GetMapping("/user")
    @Operation(
        summary = "Получить решения текущего пользователя",
        description = "Возвращает все решения, загруженные текущим пользователем (из токена)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Решения найдены"),
            ApiResponse(responseCode = "401", description = "Не авторизирован"),
            ApiResponse(responseCode = "404", description = "Решения не найдены")
        ]
    )
    fun getByUser(httpRequest: HttpServletRequest): ResponseEntity<List<SolutionResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/solutions/user - пользователь ${payload.userId} получает свои решения" }
        val solutions = solutionService.findByUser(payload.userId)
        logger.debug { "Найдено решений: ${solutions.size}" }
        return ResponseEntity.ok(solutions.map { SolutionResponse.fromEntity(it) })
    }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Обновить статус решения",
        description = "Изменяет статус проверки решения (PENDING, CHECKING, COMPLETED, PLAGIARISM_DETECTED). Только для администраторов."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Статус успешно обновлен"),
            ApiResponse(responseCode = "400", description = "Неверный статус"),
            ApiResponse(responseCode = "401", description = "Не авторизирован"),
            ApiResponse(responseCode = "403", description = "Запрещено"),
            ApiResponse(responseCode = "404", description = "Решение не найдено")
        ]
    )
    fun updateStatus(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long,
        @Parameter(description = "Новый статус", example = "COMPLETED")
        @RequestParam status: String,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SolutionResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "PATCH /api/solutions/$id/status - администратор ${payload.userId} меняет статус на $status" }
        // Только администраторы могут обновлять статус
        if (payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался обновить статус решения (требуется ADMIN)" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val solutionStatus = try {
            SolutionStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            logger.warn { "Неизвестный статус: $status" }
            throw IllegalArgumentException("Неизвестный статус: $status")
        }
        val updated = solutionService.updateStatus(id, solutionStatus)
        logger.info { "Статус решения $id обновлен на ${updated.status}" }
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
        if (solution == null) {
            return ResponseEntity.notFound().build()
        }

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