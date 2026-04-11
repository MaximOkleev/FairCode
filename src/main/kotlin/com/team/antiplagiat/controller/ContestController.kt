package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.ContestRequest
import com.team.antiplagiat.controller.dto.ContestResponse
import com.team.antiplagiat.controller.dto.toEntity
import com.team.antiplagiat.service.ContestService
import com.team.antiplagiat.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contests")
class ContestController(
    private val contestService: ContestService,
    private val userService: UserService
) {

    @PostMapping
    fun create(@RequestBody request: ContestRequest): ResponseEntity<ContestResponse> {
        val admin = userService.findById(request.adminId) ?: return ResponseEntity.badRequest().build()
        val contest = request.toEntity(admin)
        val created = contestService.create(contest) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.status(HttpStatus.CREATED).body(ContestResponse.fromEntity(created))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<ContestResponse> {
        val contest = contestService.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ContestResponse.fromEntity(contest))
    }

    @GetMapping
    fun getAll(): List<ContestResponse> = contestService.findAll().map { ContestResponse.fromEntity(it) }

    @GetMapping("/by-admin/{adminId}")
    fun getByAdmin(@PathVariable adminId: Long): List<ContestResponse> =
        contestService.findByAdmin(adminId).map { ContestResponse.fromEntity(it) }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String?,
        @RequestParam duration: Long?
    ): ResponseEntity<ContestResponse> {
        val updated = contestService.update(id, name, duration) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ContestResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        contestService.delete(id)
        return ResponseEntity.noContent().build()
    }
}