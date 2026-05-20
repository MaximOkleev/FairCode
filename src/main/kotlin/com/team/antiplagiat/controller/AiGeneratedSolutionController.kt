package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.ai.AiGeneratedSolutionResponse
import com.team.antiplagiat.controller.dto.ai.AiSolutionGenerationRequest
import com.team.antiplagiat.service.AiGeneratedSolutionService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/problems/{problemId}/ai-solutions")
class AiGeneratedSolutionController(
    private val aiGeneratedSolutionService: AiGeneratedSolutionService
) {

    @PostMapping("/generate")
    fun generate(
        @PathVariable problemId: Long,
        @RequestBody(required = false) request: AiSolutionGenerationRequest?,
        httpRequest: HttpServletRequest
    ): ResponseEntity<List<AiGeneratedSolutionResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val language = request?.language?.trim().orEmpty().ifBlank { "kotlin" }
        val generated = aiGeneratedSolutionService.generateForProblem(problemId, language)
        return ResponseEntity.status(HttpStatus.CREATED).body(generated.map { AiGeneratedSolutionResponse.fromEntity(it) })
    }

    @GetMapping
    fun getByProblem(
        @PathVariable problemId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<List<AiGeneratedSolutionResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val solutions = aiGeneratedSolutionService.findByProblem(problemId)
        return ResponseEntity.ok(solutions.map { AiGeneratedSolutionResponse.fromEntity(it) })
    }
}
