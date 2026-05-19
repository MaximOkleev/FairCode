package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayload
import com.team.antiplagiat.controller.dto.zipimport.ZipImportResponse
import com.team.antiplagiat.models.ImportJob
import com.team.antiplagiat.models.ImportJobStatus
import com.team.antiplagiat.models.User
import com.team.antiplagiat.service.ImportJobService
import com.team.antiplagiat.service.ZipImportService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(ImportController::class)
class ImportControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var zipImportService: ZipImportService

    @MockitoBean
    private lateinit var importJobService: ImportJobService

    @BeforeEach
    fun setUp() {
        reset(zipImportService)
        reset(importJobService)
    }

    @Test
    fun `importZip should return 200 for admin`() {
        val file = MockMultipartFile("file", "Solutions.zip", "application/zip", byteArrayOf(1, 2, 3))
        val job = ImportJob(
            id = 1L,
            admin = User(id = 1L, login = "admin", email = "admin@example.com"),
            fileName = "Solutions.zip",
            status = ImportJobStatus.PENDING
        )
        whenever(importJobService.createJob(1L, "Solutions.zip")).thenReturn(job)
        whenever(zipImportService.importZip(any())).thenReturn(
            ZipImportResponse(1, 2, 0, 2, 0, emptyList())
        )

        mockMvc.perform(
            multipart("/api/import/zip")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        )
            .andExpect(status().isOk)

        verify(importJobService).createJob(eq(1L), eq("Solutions.zip"))
        verify(importJobService).startJob(eq(1L), eq(1L))
        verify(importJobService).completeJob(eq(1L), eq(1L), eq(2), eq(1), eq(0), eq(2), eq(0), eq(emptyList<String>()))
    }

    @Test
    fun `importZip should return 403 for non-admin`() {
        val file = MockMultipartFile("file", "Solutions.zip", "application/zip", byteArrayOf(1, 2, 3))

        mockMvc.perform(
            multipart("/api/import/zip")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 2L, login = "user", email = "user@example.com", role = "BASIC")
                )
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `importZip should return 401 when token missing`() {
        val file = MockMultipartFile("file", "Solutions.zip", "application/zip", byteArrayOf(1, 2, 3))

        mockMvc.perform(
            multipart("/api/import/zip")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `importZip should return 400 for empty file`() {
        val file = MockMultipartFile("file", "Solutions.zip", "application/zip", byteArrayOf())

        mockMvc.perform(
            multipart("/api/import/zip")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `importZip should fail job when zip import throws`() {
        val file = MockMultipartFile("file", "Solutions.zip", "application/zip", byteArrayOf(1, 2, 3))
        val job = ImportJob(
            id = 2L,
            admin = User(id = 1L, login = "admin", email = "admin@example.com"),
            fileName = "Solutions.zip",
            status = ImportJobStatus.PENDING
        )
        whenever(importJobService.createJob(1L, "Solutions.zip")).thenReturn(job)
        whenever(zipImportService.importZip(any())).thenThrow(RuntimeException("boom"))

        mockMvc.perform(
            multipart("/api/import/zip")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        ).andExpect(status().isInternalServerError)

        verify(importJobService).failJob(eq(2L), eq(1L), eq("boom"))
    }
}
