package com.team.antiplagiat.controller

import com.team.antiplagiat.controller.dto.ProblemRequest
import com.team.antiplagiat.controller.dto.ProblemResponse
import com.team.antiplagiat.controller.dto.toEntity
import com.team.antiplagiat.service.ProblemService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/problems")
class ProblemController(private val problemService: ProblemService) {

    @PostMapping
    fun create(@RequestBody request: ProblemRequest): ResponseEntity<ProblemResponse> {
        val problem = request.toEntity()
        val saved = problemService.create(problem.name, problem.description)
        return ResponseEntity.status(HttpStatus.CREATED).body(ProblemResponse.fromEntity(saved))
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): ResponseEntity<ProblemResponse> {
        val problem = problemService.findById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ProblemResponse.fromEntity(problem))
    }

    @GetMapping
    fun getAll(): List<ProblemResponse> = problemService.findAll().map { ProblemResponse.fromEntity(it) }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: ProblemRequest): ResponseEntity<ProblemResponse> {
        val updated = problemService.update(id, request.name, request.description) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ProblemResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        problemService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
