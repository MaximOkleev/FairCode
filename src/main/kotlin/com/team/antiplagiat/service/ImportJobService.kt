package com.team.antiplagiat.service

import com.team.antiplagiat.controller.dto.zipimport.ImportJobDto
import com.team.antiplagiat.models.ImportJob
import com.team.antiplagiat.models.ImportJobStatus
import com.team.antiplagiat.repository.ImportJobRepository
import com.team.antiplagiat.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
@Suppress("unused")
class ImportJobService(
    private val importJobRepository: ImportJobRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createJob(adminId: Long, fileName: String): ImportJob {
        val admin = userRepository.findById(adminId)
            .orElseThrow { IllegalArgumentException("Admin with id=$adminId not found") }

        val job = ImportJob(
            admin = admin,
            fileName = fileName,
            status = ImportJobStatus.PENDING,
            createdAt = LocalDateTime.now(),
            startedAt = null
        )
        logger.info { "Creating import job: fileName=$fileName, adminId=${admin.id}" }
        return importJobRepository.save(job)
    }

    @Transactional
    fun startJob(jobId: Long, adminId: Long) {
        val job = importJobRepository.findByIdAndAdminId(jobId, adminId)
            ?: throw IllegalArgumentException("Import job not found or access denied")

        job.status = ImportJobStatus.IN_PROGRESS
        job.startedAt = LocalDateTime.now()
        importJobRepository.save(job)
        logger.info { "Starting import job: id=$jobId" }
    }

    @Transactional
    fun completeJob(
        jobId: Long,
        adminId: Long,
        importedSolutions: Int,
        createdProblems: Int,
        skippedFiles: Int,
        usersMatched: Int,
        usersNotFound: Int,
        errors: List<String>
    ) {
        val job = importJobRepository.findByIdAndAdminId(jobId, adminId)
            ?: throw IllegalArgumentException("Import job not found or access denied")

        job.status = ImportJobStatus.COMPLETED
        job.importedSolutions = importedSolutions
        job.createdProblems = createdProblems
        job.skippedFiles = skippedFiles
        job.usersMatched = usersMatched
        job.usersNotFound = usersNotFound
        job.errors = if (errors.isNotEmpty()) errors.joinToString("\n") else null
        job.finishedAt = LocalDateTime.now()

        importJobRepository.save(job)
        logger.info { "Completed import job: id=$jobId, solutions=$importedSolutions, errors=${errors.size}" }
    }

    @Transactional
    fun failJob(
        jobId: Long,
        adminId: Long,
        errorMessage: String
    ) {
        val job = importJobRepository.findByIdAndAdminId(jobId, adminId)
            ?: throw IllegalArgumentException("Import job not found or access denied")

        job.status = ImportJobStatus.FAILED
        job.errors = errorMessage
        job.finishedAt = LocalDateTime.now()

        importJobRepository.save(job)
        logger.info { "Failed import job: id=$jobId, error=$errorMessage" }
    }

    @Transactional(readOnly = true)
    fun getJob(jobId: Long, adminId: Long): ImportJobDto {
        val job = importJobRepository.findByIdAndAdminId(jobId, adminId)
            ?: throw IllegalArgumentException("Import job not found or access denied")

        return mapToDto(job)
    }

    @Transactional(readOnly = true)
    fun getJobHistory(adminId: Long, pageable: Pageable): Page<ImportJobDto> {
        return importJobRepository.findByAdminIdOrderByCreatedAtDesc(adminId, pageable)
            .map { mapToDto(it) }
    }

    @Transactional(readOnly = true)
    fun getJobHistory(adminId: Long): List<ImportJobDto> {
        return importJobRepository.findByAdminIdOrderByCreatedAtDesc(adminId)
            .map { mapToDto(it) }
    }

    private fun mapToDto(job: ImportJob): ImportJobDto {
        val errorsList = job.errors?.split("\n")?.filter { it.isNotBlank() } ?: emptyList()

        return ImportJobDto(
            id = job.id,
            status = job.status.name,
            fileName = job.fileName,
            createdAt = job.createdAt,
            startedAt = job.startedAt,
            finishedAt = job.finishedAt,
            importedSolutions = job.importedSolutions,
            createdProblems = job.createdProblems,
            skippedFiles = job.skippedFiles,
            usersMatched = job.usersMatched,
            usersNotFound = job.usersNotFound,
            errors = errorsList
        )
    }
}
