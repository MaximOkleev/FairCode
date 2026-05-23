package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.ai.AiPlagiarismCheckResponse
import com.team.antiplagiat.controller.dto.ai.AiPlagiarismCheckSummaryResponse
import com.team.antiplagiat.service.AiPlagiarismService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ai-plagiarism")
@Tag(name = "AI Plagiarism", description = "Проверка решений на совпадение с AI-generated решениями")
class AiPlagiarismController(
    private val aiPlagiarismService: AiPlagiarismService
) {

    @PostMapping("/solutions/{solutionId}/check")
    @Operation(
        summary = "Проверить одно решение на совпадение с AI",
        description = "Сравнивает решение текущего пользователя с сохранёнными AI-решениями той же задачи и языка."
    )
    fun checkSolutionAgainstAi(
        @PathVariable solutionId: Long,
        @RequestParam(defaultValue = "0.8") threshold: Double,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AiPlagiarismCheckResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(
            aiPlagiarismService.checkSolution(
                ownerId = payload.userId,
                solutionId = solutionId,
                threshold = threshold
            )
        )
    }

    @PostMapping("/contests/{contestId}/check")
    @Operation(
        summary = "Проверить весь контест на совпадение с AI",
        description = "Сравнивает все решения текущего пользователя в контесте с сохранёнными AI-решениями по соответствующим задачам и языкам."
    )
    fun checkContestAgainstAi(
        @PathVariable contestId: Long,
        @RequestParam(defaultValue = "0.8") threshold: Double,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AiPlagiarismCheckResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(
            aiPlagiarismService.checkContest(
                ownerId = payload.userId,
                contestId = contestId,
                threshold = threshold
            )
        )
    }

    @GetMapping("/history")
    @Operation(
        summary = "Получить историю AI-проверок",
        description = "Возвращает сохраненные AI-проверки текущего пользователя."
    )
    fun getHistory(httpRequest: HttpServletRequest): ResponseEntity<List<AiPlagiarismCheckSummaryResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(aiPlagiarismService.findHistory(payload.userId))
    }

    @GetMapping("/history/{runId}")
    @Operation(
        summary = "Получить сохраненный результат AI-проверки",
        description = "Возвращает сохраненные совпадения для AI-проверки. В истории хранятся только пары выше threshold."
    )
    fun getHistoryRun(
        @PathVariable runId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AiPlagiarismCheckResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(aiPlagiarismService.findHistoryRun(payload.userId, runId))
    }
}
