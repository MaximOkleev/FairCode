package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismContestProblemCheckResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismCheckSummaryResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismComparisonResponse
import com.team.antiplagiat.service.PlagiarismService
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
@RequestMapping("/api/plagiarism")
@Tag(name = "Plagiarism", description = "API for solution plagiarism checks")
class PlagiarismController(
    private val plagiarismService: PlagiarismService
) {

    @PostMapping("/compare")
    @Operation(
        summary = "Compare two owned solutions",
        description = "Compares any two solutions added by the current user."
    )
    fun compareSolutions(
        @RequestParam firstSolutionId: Long,
        @RequestParam secondSolutionId: Long,
        @RequestParam(defaultValue = "0.8") threshold: Double,
        httpRequest: HttpServletRequest
    ): ResponseEntity<PlagiarismComparisonResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(
            plagiarismService.compareOwnedSolutions(
                ownerId = payload.userId,
                firstSolutionId = firstSolutionId,
                secondSolutionId = secondSolutionId,
                threshold = threshold
            )
        )
    }

    @PostMapping("/contests/{contestId}/problems/{problemId}/check")
    @Operation(
        summary = "Check all solutions for a contest problem",
        description = "Compares all current user's solutions for one problem inside one contest."
    )
    fun checkContestProblem(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        @RequestParam(defaultValue = "0.8") threshold: Double,
        httpRequest: HttpServletRequest
    ): ResponseEntity<PlagiarismContestProblemCheckResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(
            plagiarismService.checkContestProblem(
                ownerId = payload.userId,
                contestId = contestId,
                problemId = problemId,
                threshold = threshold
            )
        )
    }

    @GetMapping("/history")
    @Operation(
        summary = "Get plagiarism check history",
        description = "Returns saved plagiarism checks started by the current user."
    )
    fun getHistory(httpRequest: HttpServletRequest): ResponseEntity<List<PlagiarismCheckSummaryResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(plagiarismService.findHistory(payload.userId))
    }

    @GetMapping("/history/{runId}/matches")
    @Operation(
        summary = "Get saved plagiarism matches for a check",
        description = "Returns matched pairs saved for a previous plagiarism check."
    )
    fun getHistoryMatches(
        @PathVariable runId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<List<com.team.antiplagiat.controller.dto.plagiarism.PlagiarismMatchResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return ResponseEntity.ok(plagiarismService.findHistoryMatches(payload.userId, runId))
    }

}
