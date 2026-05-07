package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.contest.ContestRequest
import com.team.antiplagiat.controller.dto.contest.ContestResponse
import com.team.antiplagiat.controller.dto.contest.toEntity
import com.team.antiplagiat.service.ContestService
import com.team.antiplagiat.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
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
    fun create(@RequestBody request: ContestRequest): ResponseEntity<ContestResponse> {
        logger.info { "POST /api/contests - создание контеста от администратора ${request.adminId}" }
        logger.debug { "Request: $request" }

        val admin = userService.findById(request.adminId) ?: run {
            logger.warn { "Администратор ${request.adminId} не найден" }
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
    fun get(@PathVariable id: Long): ResponseEntity<ContestResponse> {
        logger.debug { "GET /api/contests/$id" }
        val contest = contestService.findById(id) ?: run {
            logger.debug { "Контест $id не найден" }
            return ResponseEntity.notFound().build()
        }
        logger.debug { "Контест найден: $contest" }
        return ResponseEntity.ok(ContestResponse.fromEntity(contest))
    }

    @GetMapping
    fun getAll(): List<ContestResponse> {
        logger.debug { "GET /api/contests - получение всех контестов" }
        val contests = contestService.findAll()
        logger.debug { "Найдено контестов: ${contests.size}" }
        return contests.map { ContestResponse.fromEntity(it) }
    }

    @GetMapping("/by-admin/{adminId}")
    fun getByAdmin(@PathVariable adminId: Long): List<ContestResponse> {
        logger.debug { "GET /api/contests/by-admin/$adminId" }
        val contests = contestService.findByAdmin(adminId)
        logger.debug { "Найдено контестов для администратора: ${contests.size}" }
        return contests.map { ContestResponse.fromEntity(it) }
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String?,
        @RequestParam duration: Long?
    ): ResponseEntity<ContestResponse> {
        logger.info { "PUT /api/contests/$id - обновление: name=$name, duration=$duration" }
        logger.debug { "Параметры: id=$id, name=$name, duration=$duration" }

        val updated = contestService.update(id, name, duration) ?: run {
            logger.warn { "Контест $id не найден или ошибка обновления" }
            return ResponseEntity.notFound().build()
        }

        logger.info { "Контест обновлен: id=${updated.id}" }
        return ResponseEntity.ok(ContestResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        logger.info { "DELETE /api/contests/$id" }
        logger.debug { "Удаление контеста" }
        contestService.delete(id)
        logger.info { "Контест удален: id=$id" }
        return ResponseEntity.noContent().build()
    }
}