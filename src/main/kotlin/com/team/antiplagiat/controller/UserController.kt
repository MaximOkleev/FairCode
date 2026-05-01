package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.user.UserRequest
import com.team.antiplagiat.controller.dto.user.UserResponse
import com.team.antiplagiat.controller.dto.user.toEntity
import com.team.antiplagiat.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun create(@RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val user = request.toEntity()
        val saved = userService.create(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(saved))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<UserResponse> {
        val user = userService.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.fromEntity(user))
    }

    @GetMapping
    fun getAll(): List<UserResponse> = userService.findAll().map { UserResponse.fromEntity(it) }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: UserRequest): ResponseEntity<UserResponse> {
        val updated = userService.update(id, request.login, request.email) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(UserResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        userService.delete(id)
        return ResponseEntity.noContent().build()
    }
}