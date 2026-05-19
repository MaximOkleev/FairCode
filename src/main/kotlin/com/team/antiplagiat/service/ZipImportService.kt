package com.team.antiplagiat.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import com.team.antiplagiat.config.props.ZipImportProperties
import com.team.antiplagiat.controller.dto.zipimport.ZipImportResponse
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.SolutionStatus
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.zip.ZipInputStream

private val logger = KotlinLogging.logger {}

@Service
class ZipImportService(
    private val problemRepository: ProblemRepository,
    private val solutionRepository: SolutionRepository,
    private val userRepository: UserRepository,
    private val zipImportProperties: ZipImportProperties,
    private val meterRegistry: MeterRegistry
) {
    private data class ImportStats(
        var problemsCreated: Int = 0,
        var solutionsCreated: Int = 0,
        var skippedFiles: Int = 0,
        var usersMatched: Int = 0,
        var usersNotFound: Int = 0,
        val errors: MutableList<String> = mutableListOf()
    )

    @Transactional
    fun importZip(file: MultipartFile): ZipImportResponse {

        val stats = ImportStats()
        val maxFiles = zipImportProperties.maxFiles
        val maxEntrySizeBytes = zipImportProperties.maxEntrySize.toBytes()
        var fileEntriesSeen = 0

        ZipInputStream(file.inputStream).use { zip ->
            var entry = zip.nextEntry

            while (entry != null) {
                if (!entry.isDirectory) {
                    fileEntriesSeen++
                    processEntry(
                        zip = zip,
                        entry = entry,
                        fileEntriesSeen = fileEntriesSeen,
                        maxFiles = maxFiles,
                        maxEntrySizeBytes = maxEntrySizeBytes,
                        stats = stats
                    )
                }

                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        if (stats.solutionsCreated > 0) {
            meterRegistry.counter("zip.import.total").increment(stats.solutionsCreated.toDouble())
        }

        logger.info {
            "ZIP import completed: problemsCreated=${stats.problemsCreated}, solutionsCreated=${stats.solutionsCreated}, " +
                "usersMatched=${stats.usersMatched}, usersNotFound=${stats.usersNotFound}, skippedFiles=${stats.skippedFiles}, errors=${stats.errors.size}"
        }

        return ZipImportResponse(
            problemsCreated = stats.problemsCreated,
            solutionsCreated = stats.solutionsCreated,
            skippedFiles = stats.skippedFiles,
            usersMatched = stats.usersMatched,
            usersNotFound = stats.usersNotFound,
            errors = stats.errors
        )
    }

    private fun processEntry(
        zip: ZipInputStream,
        entry: java.util.zip.ZipEntry,
        fileEntriesSeen: Int,
        maxFiles: Int,
        maxEntrySizeBytes: Long,
        stats: ImportStats
    ) {
        val rawPath = entry.name

        try {
            if (fileEntriesSeen > maxFiles) {
                stats.skippedFiles++
                stats.errors += "Превышено максимальное количество файлов в архиве: $maxFiles"
                return
            }

            val parts = normalizeEntryPath(rawPath)
            if (parts == null) {
                stats.skippedFiles++
                stats.errors += "Опасный или некорректный путь пропущен: $rawPath"
                return
            }

            val relativeParts = if (parts.firstOrNull() == "Solutions") parts.drop(1) else parts
            if (relativeParts.size < 2) {
                stats.skippedFiles++
                return
            }

            val problemName = relativeParts.first()
            val fileName = relativeParts.last()

            if (!isAllowedSourceFile(fileName)) {
                stats.skippedFiles++
                stats.errors += "Неподдерживаемое расширение файла пропущено: $fileName"
                return
            }

            if (entry.size >= 0 && entry.size > maxEntrySizeBytes) {
                stats.skippedFiles++
                stats.errors += "Файл слишком большой и был пропущен: $rawPath (лимит ${zipImportProperties.maxEntrySize.toMegabytes()}MB)"
                return
            }

            val content = readEntryText(zip, maxEntrySizeBytes)
            if (content == null) {
                stats.skippedFiles++
                stats.errors += "Файл слишком большой и был пропущен: $rawPath (лимит ${zipImportProperties.maxEntrySize.toMegabytes()}MB)"
                return
            }

            // Извлекаем login из имени файла (без расширения)
            val login = fileName.substringBeforeLast(".")
            val user = userRepository.findByLogin(login)
            if (user == null) {
                stats.skippedFiles++
                stats.usersNotFound++
                stats.errors += "Пользователь с логином '$login' не найден в системе (файл: $fileName)"
                return
            }

            val problem = problemRepository.findFirstByName(problemName)
                ?: problemRepository.save(
                    Problem(
                        name = problemName,
                        description = "Imported from ZIP"
                    )
                ).also {
                    stats.problemsCreated++
                }

            val solution = Solution(
                user = user,
                problem = problem,
                language = detectLanguage(fileName),
                status = SolutionStatus.WAITING,
                submittedAt = LocalDateTime.now(),
                filePath = relativeParts.joinToString("/"),
                code = content
            )

            solutionRepository.save(solution)
            stats.usersMatched++
            stats.solutionsCreated++
        } catch (ex: Exception) {
            stats.skippedFiles++
            stats.errors += "Ошибка при обработке $rawPath: ${ex.message}"
        }
    }

    private fun normalizeEntryPath(rawPath: String): List<String>? {
        val normalized = rawPath
            .replace('\\', '/')
            .trimStart('/')
            .removePrefix("./")

        val parts = normalized.split('/')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (parts.isEmpty()) return null
        if (parts.any { it == ".." || it.contains(':') }) return null

        return parts
    }

    private fun readEntryText(zip: ZipInputStream, maxBytes: Long): String? {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L
        var tooLarge = false

        while (true) {
            val read = zip.read(buffer)
            if (read <= 0) break

            total += read
            if (total > maxBytes) {
                tooLarge = true
                break
            }

            output.write(buffer, 0, read)
        }

        if (tooLarge) {
            while (zip.read(buffer) > 0) {
                // drain current entry
            }
            return null
        }

        return output.toString(Charsets.UTF_8.name())
    }

    private fun isAllowedSourceFile(fileName: String): Boolean {
        return fileName.endsWith(".cpp", true) ||
                fileName.endsWith(".java", true) ||
                fileName.endsWith(".kt", true) ||
                fileName.endsWith(".py", true) ||
                fileName.endsWith(".cs", true)
    }

    private fun detectLanguage(fileName: String): String {
        return when {
            fileName.endsWith(".cpp", true) -> "C++"
            fileName.endsWith(".java", true) -> "Java"
            fileName.endsWith(".kt", true) -> "Kotlin"
            fileName.endsWith(".py", true) -> "Python"
            fileName.endsWith(".cs", true) -> "C#"
            else -> "Unknown"
        }
    }
}

