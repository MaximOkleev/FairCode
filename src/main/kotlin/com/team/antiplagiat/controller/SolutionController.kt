package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.solution.SolutionRequest
import com.team.antiplagiat.controller.dto.solution.SolutionResponse
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.service.SolutionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/solutions")
@Tag(name = "Solutions", description = "API для управления решениями код-документов")
class SolutionController(private val solutionService: SolutionService) {

    @PostMapping
    @Operation(
        summary = "Создать новое решение",
        description = "Загружает новое решение для проверки на плагиат. Требует userId, problemId, language и filePath."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Решение успешно создано"),
            ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        ]
    )
    fun create(@Valid @RequestBody request: SolutionRequest): ResponseEntity<SolutionResponse> {
        if (request.userId <= 0 || request.problemId <= 0) {
            return ResponseEntity.badRequest().build()
        }
        val solution = solutionService.create(
            userId = request.userId,
            problemId = request.problemId,
            language = request.language,
            filePath = request.filePath,
            code = request.code
        )
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
            ApiResponse(responseCode = "404", description = "Решение не найдено"),
            ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
        ]
    )
    fun get(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<SolutionResponse> {
        val solution = solutionService.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(SolutionResponse.fromEntity(solution))
    }

    @GetMapping
    @Operation(
        summary = "Получить все решения",
        description = "Возвращает список всех решений в системе"
    )
    @ApiResponse(responseCode = "200", description = "Список решений успешно получен")
    fun getAll(): List<SolutionResponse> =
        solutionService.findAll().map { SolutionResponse.fromEntity(it) }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Получить решения пользователя",
        description = "Возвращает все решения, загруженные конкретным пользователем"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Решения найдены"),
            ApiResponse(responseCode = "404", description = "Пользователь не найден")
        ]
    )
    fun getByUser(
        @Parameter(description = "ID пользователя", example = "1")
        @PathVariable userId: Long
    ): List<SolutionResponse> =
        solutionService.findByUser(userId).map { SolutionResponse.fromEntity(it) }

    @PatchMapping("/{id}/status")
    @Operation(
        summary = "Обновить статус решения",
        description = "Изменяет статус проверки решения (PENDING, CHECKING, COMPLETED, PLAGIARISM_DETECTED)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Статус успешно обновлен"),
            ApiResponse(responseCode = "400", description = "Неверный статус"),
            ApiResponse(responseCode = "404", description = "Решение не найдено")
        ]
    )
    fun updateStatus(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long,
        @Parameter(description = "Новый статус", example = "COMPLETED")
        @RequestParam status: String
    ): ResponseEntity<SolutionResponse> {
        val solutionStatus = try {
            SolutionStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Неизвестный статус: $status")
        }
        val updated = solutionService.updateStatus(id, solutionStatus)
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
            ApiResponse(responseCode = "404", description = "Решение не найдено")
        ]
    )
    fun delete(
        @Parameter(description = "ID решения", example = "1")
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        solutionService.delete(id)
        return ResponseEntity.noContent().build()
    }
}