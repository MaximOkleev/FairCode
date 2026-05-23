package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayloadExtractor
import com.team.antiplagiat.controller.dto.contest.ContestRequest
import com.team.antiplagiat.controller.dto.contest.ContestResponse
import com.team.antiplagiat.controller.dto.contest.toEntity
import com.team.antiplagiat.controller.dto.problem.ProblemRequest
import com.team.antiplagiat.controller.dto.problem.ProblemResponse
import com.team.antiplagiat.controller.dto.solution.SolutionResponse
import com.team.antiplagiat.controller.dto.solution.SolutionTextRequest
import com.team.antiplagiat.controller.dto.zipimport.ZipImportResponse
import com.team.antiplagiat.service.ContestService
import com.team.antiplagiat.service.SolutionService
import com.team.antiplagiat.service.UserService
import com.team.antiplagiat.service.ZipImportService
import io.swagger.v3.oas.annotations.Operation
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/contests")
class ContestController(
    private val contestService: ContestService,
    private val solutionService: SolutionService,
    private val zipImportService: ZipImportService,
    private val userService: UserService
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: ContestRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContestResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.info { "POST /api/contests - создание контеста пользователем ${payload.userId}" }
        logger.debug { "Request: $request" }

        val admin = userService.findById(payload.userId) ?: run {
            logger.warn { "Пользователь ${payload.userId} не найден" }
            return ResponseEntity.badRequest().build()
        }

        val contest = request.toEntity(admin)
        val created = contestService.create(contest) ?: run {
            logger.warn { "Ошибка создания контеста" }
            return ResponseEntity.badRequest().build()
        }

        logger.info { "Контест создан: id=${created.id}" }
        return ResponseEntity.status(HttpStatus.CREATED).body(ContestResponse.fromEntity(created))
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContestResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/contests/$id - пользователь ${payload.userId}" }
        val contest = contestService.findByIdAndOwner(id, payload.userId) ?: run {
            logger.debug { "Контест $id не найден" }
            return ResponseEntity.notFound().build()
        }
        logger.debug { "Контест найден: $contest" }
        return ResponseEntity.ok(ContestResponse.fromEntity(contest))
    }

    @PostMapping("/{contestId}/problems")
    fun createProblem(
        @PathVariable contestId: Long,
        @Valid @RequestBody request: ProblemRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ProblemResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val problem = contestService.createProblemInContest(
            contestId = contestId,
            ownerId = payload.userId,
            name = request.name,
            description = request.description,
            condition = request.condition
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(ProblemResponse.fromEntity(problem))
    }

    @PostMapping("/{contestId}/problems/{problemId}")
    fun addProblem(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ProblemResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val problem = contestService.addProblem(contestId, payload.userId, problemId)
        return ResponseEntity.ok(ProblemResponse.fromEntity(problem))
    }

    @GetMapping("/{contestId}/problems/{problemId}/solutions")
    @Operation(summary = "Получить решения конкретной задачи в контесте")
    fun getProblemSolutions(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<List<SolutionResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        contestService.findOwnedProblem(contestId, payload.userId, problemId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            solutionService.findByUserAndContest(payload.userId, contestId)
                .filter { it.problem.id == problemId }
                .map { SolutionResponse.fromEntity(it) }
        )
    }

    @PostMapping("/{contestId}/problems/{problemId}/solutions/text")
    @Operation(summary = "Добавить решение конкретной задачи текстом")
    fun createTextSolution(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        @Valid @RequestBody request: SolutionTextRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SolutionResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val solution = solutionService.createInContest(
            ownerId = payload.userId,
            contestId = contestId,
            problemId = problemId,
            language = request.language,
            filePath = request.filePath,
            code = request.code
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(SolutionResponse.fromEntity(solution))
    }

    @PostMapping(
        "/{contestId}/problems/{problemId}/solutions/file",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(summary = "Загрузить решение конкретной задачи файлом")
    fun uploadSolutionFile(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) language: String?,
        @RequestParam(required = false) filePath: String?,
        httpRequest: HttpServletRequest
    ): ResponseEntity<SolutionResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (file.isEmpty) {
            throw IllegalArgumentException("Solution file is empty")
        }

        val originalFilename = file.originalFilename?.takeIf { it.isNotBlank() } ?: file.name
        val solution = solutionService.createInContest(
            ownerId = payload.userId,
            contestId = contestId,
            problemId = problemId,
            language = language?.takeIf { it.isNotBlank() } ?: detectLanguage(originalFilename),
            filePath = filePath?.takeIf { it.isNotBlank() } ?: originalFilename,
            code = file.bytes.toString(Charsets.UTF_8)
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(SolutionResponse.fromEntity(solution))
    }

    @PostMapping(
        "/{contestId}/problems/{problemId}/solutions/zip",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    @Operation(summary = "Загрузить ZIP-архив решений конкретной задачи")
    fun uploadSolutionZip(
        @PathVariable contestId: Long,
        @PathVariable problemId: Long,
        @RequestParam("file") file: MultipartFile,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ZipImportResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (file.isEmpty) {
            throw IllegalArgumentException("ZIP file is empty")
        }

        val contestProblem = contestService.findOwnedProblem(contestId, payload.userId, problemId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(
            zipImportService.importZipForContestProblem(
                file = file,
                owner = contestProblem.contest.admin,
                contest = contestProblem.contest,
                problem = contestProblem.problem
            )
        )
    }

    @GetMapping
    fun getAll(httpRequest: HttpServletRequest): ResponseEntity<List<ContestResponse>> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        logger.debug { "GET /api/contests - пользователь ${payload.userId}" }
        val contests = contestService.findByAdmin(payload.userId)
        logger.debug { "Найдено контестов: ${contests.size}" }
        return ResponseEntity.ok(contests.map { ContestResponse.fromEntity(it) })
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestParam name: String?,
        @RequestParam duration: Long?,
        httpRequest: HttpServletRequest
    ): ResponseEntity<ContestResponse> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (contestService.findByIdAndOwner(id, payload.userId) == null) {
            return ResponseEntity.notFound().build()
        }

        logger.info { "PUT /api/contests/$id - обновление пользователем ${payload.userId}: name=$name, duration=$duration" }
        logger.debug { "Параметры: id=$id, name=$name, duration=$duration" }

        val updated = contestService.update(id, name, duration) ?: run {
            logger.warn { "Контест $id не найден или ошибка обновления" }
            return ResponseEntity.notFound().build()
        }

        logger.info { "Контест обновлен: id=${updated.id}" }
        return ResponseEntity.ok(ContestResponse.fromEntity(updated))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val payload = TokenPayloadExtractor.getTokenPayload(httpRequest)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        if (contestService.findByIdAndOwner(id, payload.userId) == null) {
            return ResponseEntity.notFound().build()
        }

        logger.info { "DELETE /api/contests/$id - удаление пользователем ${payload.userId}" }
        logger.debug { "Удаление контеста" }
        contestService.delete(id)
        logger.info { "Контест удален: id=$id" }
        return ResponseEntity.noContent().build()
    }

    private fun detectLanguage(fileName: String): String = when {
        fileName.endsWith(".cpp", true) -> "C++"
        fileName.endsWith(".java", true) -> "Java"
        fileName.endsWith(".kt", true) -> "Kotlin"
        fileName.endsWith(".py", true) -> "Python"
        fileName.endsWith(".cs", true) -> "C#"
        else -> "Unknown"
    }
}
