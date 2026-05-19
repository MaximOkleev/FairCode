package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.zipimport.ImportJobDto
import com.team.antiplagiat.controller.dto.zipimport.ZipImportResponse
import com.team.antiplagiat.service.ImportJobService
import com.team.antiplagiat.service.ZipImportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import com.team.antiplagiat.exception.ImportFailedException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/import")
@Tag(name = "Import", description = "API for ZIP imports of problems and solutions")
@Suppress("unused")
class ImportController(
    private val zipImportService: ZipImportService,
    private val importJobService: ImportJobService
) {
    @PostMapping("/zip", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Import solutions from ZIP archive",
        description = "Supported archive structure:\n" +
            "Solutions/\n" +
            "  Problem1/\n" +
            "    ivan.cpp\n" +
            "    petr.cpp\n\n" +
            "Notes:\n" +
            "- Each file name (text before the last dot) is treated as a user login (e.g. 'ivan.cpp' → login='ivan').\n" +
            "- If a user with that login is not found, the file is skipped and an error is returned in the response.\n" +
            "- Allowed extensions: .cpp, .java, .kt, .py, .cs.\n"
    )
    fun importZip(
        @RequestParam("file") file: MultipartFile,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ZipImportResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        if (file.isEmpty) {
            throw IllegalArgumentException("ZIP file is empty")
        }

        val fileName = file.originalFilename ?: file.name
        val job = importJobService.createJob(payload.userId, fileName)
        importJobService.startJob(job.id, payload.userId)

        return try {
            val response = zipImportService.importZip(file)
            importJobService.completeJob(
                jobId = job.id,
                adminId = payload.userId,
                importedSolutions = response.solutionsCreated,
                createdProblems = response.problemsCreated,
                skippedFiles = response.skippedFiles,
                usersMatched = response.usersMatched,
                usersNotFound = response.usersNotFound,
                errors = response.errors
            )
            ResponseEntity.ok(response)
        } catch (ex: Exception) {
            importJobService.failJob(job.id, payload.userId, ex.message ?: "ZIP import failed")
            // Оборачиваем оригинальное исключение в ImportFailedException, чтобы глобальный обработчик
            // вернул понятный JSON с message и traceId, а не пустой 500.
            throw ImportFailedException(ex.message ?: "ZIP import failed", ex)
        }
    }

    @GetMapping("/jobs/{jobId}")
    @Operation(summary = "Get status of import job")
    fun getImportJobStatus(
        @PathVariable jobId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ImportJobDto> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val job = importJobService.getJob(jobId, payload.userId)
        return ResponseEntity.ok(job)
    }

    @GetMapping("/jobs/history")
    @Operation(summary = "Get import job history for current admin")
    fun getImportJobHistory(
        pageable: Pageable,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Page<ImportJobDto>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (payload.role != "ADMIN") {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val history = importJobService.getJobHistory(payload.userId, pageable)
        return ResponseEntity.ok(history)
    }
}
