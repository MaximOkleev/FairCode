package com.team.antiplagiat.controller.SolutionController

import com.team.antiplagiat.controller.dto.GetCountResponse
import com.team.antiplagiat.controller.dto.NewSolutionRequest
import com.team.antiplagiat.controller.dto.NewSolutionResponse
import com.team.antiplagiat.controller.dto.toEntity
import com.team.antiplagiat.controller.dto.toResponse
import com.team.antiplagiat.repository.SolutionRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/solutions")
class SolutionController(private val solutionRepository: SolutionRepository) {

    @PostMapping
    fun newSolution(@RequestBody request: NewSolutionRequest): ResponseEntity<NewSolutionResponse> {
        val solutionId = solutionRepository.add(request.toEntity())
        return if (solutionId != 0L) {
            ResponseEntity.status(HttpStatus.CREATED).body(NewSolutionResponse(solutionId))
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(NewSolutionResponse(-1))
        }
    }

//    Пример для запроса
//    {
//        "userId" : 101,
//        "taskId" : 202,
//        "language" : "kotlin",
//        "filePath" : "path"
//    }



    @GetMapping("/{id}")
    fun getSolutionById(@PathVariable id: Long): ResponseEntity<Any> {
        val solution = solutionRepository.findById(id)
        return if (solution != null) {
            ResponseEntity.ok().body(solution.toResponse())
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solution with id = $id not found")
        }
    }

    @GetMapping("/all")
    fun getAllSolutions(): ResponseEntity<Any> {
        val solutions = solutionRepository.findAll()
        return if (solutions.isEmpty()) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solution list is empty")
        } else {
            ResponseEntity.ok().body(solutions.map { it.toResponse() })
        }
    }

    @GetMapping("/count")
    fun getSolutionsCount(): ResponseEntity<GetCountResponse> {
        val count = solutionRepository.count()
        return ResponseEntity.ok().body(GetCountResponse(count))
    }
}