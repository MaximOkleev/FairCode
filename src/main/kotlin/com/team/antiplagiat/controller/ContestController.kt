package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.contest.ContestRequest
import com.team.antiplagiat.controller.dto.contest.ContestResponse
import com.team.antiplagiat.controller.dto.contest.toEntity
import com.team.antiplagiat.service.ContestService
import com.team.antiplagiat.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/contests")
class ContestController(
    private val contestService: ContestService,
    private val userService: UserService
) {

    @PostMapping
    fun create(
        @RequestBody request: ContestRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContestResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            logger.warn { "POST /api/contests - доступ запрещён пользователю ${payload.userId} (роль: ${payload.role})" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        logger.info { "POST /api/contests - создание контеста от администратора ${payload.userId}" }
        logger.debug { "Request: $request" }

        val admin = userService.findById(payload.userId) ?: run {
            logger.warn { "Администратор ${payload.userId} не найден" }
            return ResponseEntity.badRequest().build()
        }

        val contest = request.toEntity(admin)
        val created = contestService.create(contest) ?: run {
            logger.warn { "Ошибка создания контеста" }
            return ResponseEntity.badRequest().build()
        }

        logger.info { "Контест создан: id=${created.id}" }
        return ResponseEntity.status(HttpStatus.CREATED).body(ContestResponse.fromEntity(created))
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContestResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/contests/$id - пользователь ${payload.userId}" }
        val contest = contestService.findById(id) ?: run {
            logger.debug { "Контест $id не найден" }
            return ResponseEntity.notFound().build()
        }
        logger.debug { "Контест найден: $contest" }
        return ResponseEntity.ok(ContestResponse.fromEntity(contest))
    }

    @GetMapping
    fun getAll(httpRequest: HttpServletRequest): ResponseEntity<List<ContestResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/contests - пользователь ${payload.userId}" }
        val contests = contestService.findAll()
        logger.debug { "Найдено контестов: ${contests.size}" }
        return ResponseEntity.ok(contests.map { ContestResponse.fromEntity(it) })
    }

    @GetMapping("/by-admin")
    fun getByAdmin(httpRequest: HttpServletRequest): ResponseEntity<List<ContestResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "GET /api/contests/by-admin - фильтр за администратором ${payload.userId}" }
        val contests = contestService.findByAdmin(payload.userId)
        logger.debug { "Найдено контестов для администратора: ${contests.size}" }
        return ResponseEntity.ok(contests.map { ContestResponse.fromEntity(it) })
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String?,
        @RequestParam duration: Long?,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContestResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            logger.warn { "PUT /api/contests/$id - доступ запрещён пользователю ${payload.userId} (роль: ${payload.role})" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        logger.info { "PUT /api/contests/$id - обновление от администратора ${payload.userId}: name=$name, duration=$duration" }
        logger.debug { "Параметры: id=$id, name=$name, duration=$duration" }

        val updated = contestService.update(id, name, duration) ?: run {
            logger.warn { "Контест $id не найден или ошибка обновления" }
            return ResponseEntity.notFound().build()
        }

        logger.info { "Контест обновлен: id=${updated.id}" }
        return ResponseEntity.ok(ContestResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            logger.warn { "DELETE /api/contests/$id - доступ запрещён пользователю ${payload.userId} (роль: ${payload.role})" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        logger.info { "DELETE /api/contests/$id - удаление от администратора ${payload.userId}" }
        logger.debug { "Удаление контеста" }
        contestService.delete(id)
        logger.info { "Контест удален: id=$id" }
        return ResponseEntity.noContent().build()
    }
}