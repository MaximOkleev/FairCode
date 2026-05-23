package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.problem.ProblemRequest
import com.team.antiplagiat.controller.dto.problem.ProblemResponse
import com.team.antiplagiat.service.ProblemService
import io.swagger.v3.oas.annotations.Operation
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/problems")
class ProblemController(private val problemService: ProblemService) {

    @GetMapping("/{id}")
    @Operation(summary = "Получить задачу по ID")
    fun get(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ProblemResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/problems/$id - пользователь ${payload.userId}" }
        val problem = problemService.findById(id) ?: return ResponseEntity.notFound().build()
        logger.debug { "Задача $id получена" }
        return ResponseEntity.ok(ProblemResponse.fromEntity(problem))
    }

    @PutMapping("/{id}")
    @Operation(summary = "Изменить задачу по ID")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: ProblemRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ProblemResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "PUT /api/problems/$id - обновление от пользователя ${payload.userId}" }

        val updated = problemService.update(id, request.name, request.description, request.condition)
            ?: return ResponseEntity.notFound().build()
        logger.info { "Задача $id обновлена" }
        return ResponseEntity.ok(ProblemResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить задачу по ID вместе с загруженными решениями")
    fun delete(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "DELETE /api/problems/$id - удаление пользователем ${payload.userId}" }

        problemService.delete(id)
        logger.info { "Задача $id удалена" }
        return ResponseEntity.noContent().build()
    }
}
