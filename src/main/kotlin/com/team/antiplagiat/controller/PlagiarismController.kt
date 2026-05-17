package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismCheckSummaryResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismGroupResponse
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

    @PostMapping("/check")
    @Operation(
        summary = "Run full plagiarism check",
        description = "Admin endpoint. Recalculates matches between solutions for the same problem and language."
    )
    fun runFullCheck(
        @RequestParam(defaultValue = "0.8") threshold: Double,
        httpRequest: HttpServletRequest
    ): ResponseEntity<PlagiarismCheckSummaryResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(plagiarismService.startFullCheck(threshold))
    }

    @GetMapping("/runs/{runId}")
    @Operation(
        summary = "Get plagiarism check run",
        description = "Returns status and summary counters for a plagiarism check run."
    )
    fun getRun(
        @PathVariable runId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<PlagiarismCheckSummaryResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(plagiarismService.findRun(runId))
    }

    @GetMapping("/cheaters")
    @Operation(
        summary = "Get plagiarism groups",
        description = "Returns saved suspicious groups built with DSU from matched solution pairs."
    )
    fun getCheaterGroups(httpRequest: HttpServletRequest): ResponseEntity<List<PlagiarismGroupResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        return ResponseEntity.ok(plagiarismService.findCheaterGroups())
    }
}
