package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.user.UserRequest
import com.team.antiplagiat.controller.dto.user.UserResponse
import com.team.antiplagiat.service.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {


    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<UserResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/users/$id - пользователь ${payload.userId}" }
        // Пользователь может получить только свой профиль или администратор может получить чужой
        if (id != payload.userId && payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался получить профиль другого пользователя (id=$id)" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val user = userService.findById(id) ?: return ResponseEntity.notFound().build()
        logger.debug { "Профиль пользователя $id получен" }
        return ResponseEntity.ok(UserResponse.fromEntity(user))
    }

    @GetMapping
    fun getAll(httpRequest: HttpServletRequest): ResponseEntity<List<UserResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/users - пользователь ${payload.userId}" }
        // Только администраторы могут получить список всех пользователей
        if (payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался получить список всех пользователей (требуется ADMIN)" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        logger.debug { "Администратор ${payload.userId} получает список пользователей" }
        return ResponseEntity.ok(userService.findAll().map { UserResponse.fromEntity(it) })
    }

     @PutMapping("/{id}")
     fun update(
         @PathVariable id: Long,
         @Valid @RequestBody request: UserRequest,
         httpRequest: HttpServletRequest
     ): ResponseEntity<UserResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "PUT /api/users/$id - обновление от пользователя ${payload.userId}" }
        // Пользователь может обновить только свой профиль или администратор может обновить чужой
        if (id != payload.userId && payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался обновить профиль другого пользователя (id=$id)" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val updated = userService.update(id, request.login, request.email) ?: return ResponseEntity.notFound().build()
        logger.info { "Профиль пользователя $id обновлен" }
        return ResponseEntity.ok(UserResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "DELETE /api/users/$id - удаление от пользователя ${payload.userId}" }
        // Пользователь может удалить только свой профиль или администратор может удалить чужой
        if (id != payload.userId && payload.role != "ADMIN") {
            logger.warn { "Пользователь ${payload.userId} попытался удалить профиль другого пользователя (id=$id)" }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        userService.delete(id)
        logger.info { "Профиль пользователя $id удален" }
        return ResponseEntity.noContent().build()
    }
}