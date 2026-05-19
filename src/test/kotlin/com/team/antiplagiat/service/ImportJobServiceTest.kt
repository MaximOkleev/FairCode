package com.team.antiplagiat.service

import com.team.antiplagiat.models.ImportJob
import com.team.antiplagiat.models.ImportJobStatus
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.ImportJobRepository
import com.team.antiplagiat.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ImportJobServiceTest {

    @Test
    fun `createJob should create import job with PENDING status`() {
        // Arrange
        val repository = mock<ImportJobRepository> {
            whenever(mock.save(any())).thenAnswer { invocation ->
                val job = invocation.arguments[0] as ImportJob
                job.id = 1L
                job
            }
        }
        val userRepository = mock<UserRepository> {
            whenever(mock.findById(1L)).thenReturn(java.util.Optional.of(User(id = 1L, login = "admin", email = "admin@example.com")))
        }
        val service = ImportJobService(repository, userRepository)

        // Act
        val job = service.createJob(1L, "solutions.zip")

        // Assert
        assertEquals(1L, job.id)
        assertEquals(1L, job.admin.id)
        assertEquals("solutions.zip", job.fileName)
        assertEquals(ImportJobStatus.PENDING, job.status)
        assertEquals(0, job.importedSolutions)
    }

    @Test
    fun `completeJob should set status to COMPLETED and save results`() {
        // Arrange
        val existingJob = ImportJob(
            id = 1L,
            fileName = "solutions.zip",
            status = ImportJobStatus.IN_PROGRESS
        )

        val repository = mock<ImportJobRepository> {
            whenever(mock.findByIdAndAdminId(1L, 1L)).thenReturn(existingJob)
            whenever(mock.save(any())).thenAnswer { invocation ->
                invocation.arguments[0] as ImportJob
            }
        }
        val userRepository = mock<UserRepository>()
        val service = ImportJobService(repository, userRepository)

        // Act
        service.completeJob(1L, 1L, 5, 2, 1, listOf("error1", "error2"))

        // Assert
        assertEquals(ImportJobStatus.COMPLETED, existingJob.status)
        assertEquals(5, existingJob.importedSolutions)
        assertEquals(2, existingJob.createdProblems)
        assertEquals(1, existingJob.skippedFiles)
        assertNotNull(existingJob.errors)
    }

    @Test
    fun `getJob should return ImportJobDto with parsed errors`() {
        // Arrange
        val job = ImportJob(
            id = 1L,
            fileName = "solutions.zip",
            status = ImportJobStatus.COMPLETED,
            importedSolutions = 3,
            errors = "error1\nerror2"
        )

        val repository = mock<ImportJobRepository> {
            whenever(mock.findByIdAndAdminId(1L, 1L)).thenReturn(job)
        }
        val userRepository = mock<UserRepository>()
        val service = ImportJobService(repository, userRepository)

        // Act
        val dto = service.getJob(1L, 1L)

        // Assert
        assertEquals(1L, dto.id)
        assertEquals("COMPLETED", dto.status)
        assertEquals(3, dto.importedSolutions)
        assertEquals(2, dto.errors.size)
        assertEquals("error1", dto.errors[0])
        assertEquals("error2", dto.errors[1])
    }
}
