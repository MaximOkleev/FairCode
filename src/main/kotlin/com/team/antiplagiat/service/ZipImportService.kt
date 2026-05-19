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
    @Transactional
    fun importZip(file: MultipartFile): ZipImportResponse {

        var problemsCreated = 0
        var solutionsCreated = 0
        var skippedFiles = 0
        var usersMatched = 0
        var usersNotFound = 0
        val errors = mutableListOf<String>()
        val maxFiles = zipImportProperties.maxFiles
        val maxEntrySizeBytes = zipImportProperties.maxEntrySize.toBytes()
        var fileEntriesSeen = 0

        ZipInputStream(file.inputStream).use { zip ->
            var entry = zip.nextEntry

            while (entry != null) {
                val rawPath = entry.name

                try {
                    if (!entry.isDirectory) {
                        fileEntriesSeen++
                        if (fileEntriesSeen > maxFiles) {
                            skippedFiles++
                            errors += "Превышено максимальное количество файлов в архиве: $maxFiles"
                        } else {
                            val parts = normalizeEntryPath(rawPath)
                            if (parts == null) {
                                skippedFiles++
                                errors += "Опасный или некорректный путь пропущен: $rawPath"
                            } else {
                                val relativeParts = if (parts.firstOrNull() == "Solutions") parts.drop(1) else parts
                                if (relativeParts.size < 2) {
                                    skippedFiles++
                                } else {
                                    val problemName = relativeParts.first()
                                    val fileName = relativeParts.last()

                                    if (!isAllowedSourceFile(fileName)) {
                                        skippedFiles++
                                        errors += "Неподдерживаемое расширение файла пропущено: $fileName"
                                    } else if (entry.size >= 0 && entry.size > maxEntrySizeBytes) {
                                        skippedFiles++
                                        errors += "Файл слишком большой и был пропущен: $rawPath (лимит ${zipImportProperties.maxEntrySize.toMegabytes()}MB)"
                                    } else {
                                        val content = readEntryText(zip, maxEntrySizeBytes)
                                        if (content == null) {
                                            skippedFiles++
                                            errors += "Файл слишком большой и был пропущен: $rawPath (лимит ${zipImportProperties.maxEntrySize.toMegabytes()}MB)"
                                        } else {
                                            // Извлекаем login из имени файла (без расширения)
                                            val login = fileName.substringBeforeLast(".")
                                            val user = userRepository.findByLogin(login)
                                            if (user == null) {
                                                skippedFiles++
                                                usersNotFound++
                                                errors += "Пользователь с логином '$login' не найден в системе (файл: $fileName)"
                                            } else {
                                                usersMatched++

                                                val problem = problemRepository.findFirstByName(problemName)
                                                    ?: problemRepository.save(
                                                        Problem(
                                                            name = problemName,
                                                            description = "Imported from ZIP"
                                                        )
                                                    ).also {
                                                        problemsCreated++
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
                                                solutionsCreated++
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (ex: Exception) {
                    skippedFiles++
                    errors += "Ошибка при обработке $rawPath: ${ex.message}"
                } finally {
                    zip.closeEntry()
                }

                entry = zip.nextEntry
            }
        }

        if (solutionsCreated > 0) {
            meterRegistry.counter("zip.import.total").increment(solutionsCreated.toDouble())
        }

        logger.info {
            "ZIP import completed: problemsCreated=$problemsCreated, solutionsCreated=$solutionsCreated, " +
            "usersMatched=$usersMatched, usersNotFound=$usersNotFound, skippedFiles=$skippedFiles, errors=${errors.size}"
        }

        return ZipImportResponse(
            problemsCreated = problemsCreated,
            solutionsCreated = solutionsCreated,
            skippedFiles = skippedFiles,
            usersMatched = usersMatched,
            usersNotFound = usersNotFound,
            errors = errors
        )
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

