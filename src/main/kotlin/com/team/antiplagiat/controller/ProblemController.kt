package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.problem.ProblemRequest
import com.team.antiplagiat.controller.dto.problem.ProblemResponse
import com.team.antiplagiat.controller.dto.problem.toEntity
import com.team.antiplagiat.service.ProblemService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/problems")
class ProblemController(private val problemService: ProblemService) {

     @PostMapping
     fun create(
         @Valid @RequestBody request: ProblemRequest,
         httpRequest: HttpServletRequest
     ): ResponseEntity<ProblemResponse> {
         val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
             ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
         if (payload.role != "ADMIN") {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
         }

         logger.info { "POST /api/problems - создание от администратора ${payload.userId}" }
         // ...existing code...

         val problem = request.toEntity()
         val saved = problemService.create(problem.name, problem.description)
         logger.info { "Задача создана: id=${saved.id}" }
         return ResponseEntity.status(HttpStatus.CREATED).body(ProblemResponse.fromEntity(saved))
     }

    @GetMapping("/{id}")
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

    @GetMapping
    fun getAll(httpRequest: HttpServletRequest): ResponseEntity<List<ProblemResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/problems - пользователь ${payload.userId}" }
        logger.debug { "Получение всех задач" }
        return ResponseEntity.ok(problemService.findAll().map { ProblemResponse.fromEntity(it) })
    }

     @PutMapping("/{id}")
     fun update(
         @PathVariable id: Long,
         @Valid @RequestBody request: ProblemRequest,
         httpRequest: HttpServletRequest
     ): ResponseEntity<ProblemResponse> {
         val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
             ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
         if (payload.role != "ADMIN") {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
         }

         logger.info { "PUT /api/problems/$id - обновление от администратора ${payload.userId}" }

         val updated = problemService.update(id, request.name, request.description) ?: return ResponseEntity.notFound().build()
         logger.info { "Задача $id обновлена" }
         return ResponseEntity.ok(ProblemResponse.fromEntity(updated))
     }

     @DeleteMapping("/{id}")
     fun delete(
         @PathVariable id: Long,
         httpRequest: HttpServletRequest
     ): ResponseEntity<Void> {
         val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
             ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
         if (payload.role != "ADMIN") {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
         }

         logger.info { "DELETE /api/problems/$id - удаление администратором ${payload.userId}" }

         problemService.delete(id)
         logger.info { "Задача $id удалена" }
         return ResponseEntity.noContent().build()
     }
}
