package com.team.antiplagiat.controller

import com.team.antiplagiat.config.TokenPayload
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismSimpleResultResponse
import com.team.antiplagiat.controller.dto.plagiarism.PlagiarismCheckSummaryResponse
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.service.PlagiarismService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime


@WebMvcTest(PlagiarismController::class)
class PlagiarismControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var plagiarismService: PlagiarismService

    @BeforeEach
    fun setUp() {
        reset(plagiarismService)
    }

    @Test
    fun `getResults should return 200 for admin`() {
        whenever(plagiarismService.findLatestResults()).thenReturn(
            listOf(PlagiarismSimpleResultResponse("1 задача", "ivan", "petr", 91.4))
        )

        mockMvc.perform(
            get("/api/plagiarism/results")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        ).andExpect(status().isOk)
    }

    @Test
    fun `getResults should return 403 for non-admin`() {
        mockMvc.perform(
            get("/api/plagiarism/results")
                .contentType(MediaType.APPLICATION_JSON)
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 2L, login = "user", email = "user@example.com", role = "BASIC")
                )
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `getResults should return 401 when token missing`() {
        mockMvc.perform(
            get("/api/plagiarism/results")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `runFullCheck should return 202 for admin`() {
        val summary = PlagiarismCheckSummaryResponse(
            runId = 1L,
            status = PlagiarismCheckRunStatus.COMPLETED,
            checkedSolutions = 0,
            comparedPairs = 0,
            matches = 0,
            groups = 0,
            threshold = 0.8,
            errorMessage = null,
            createdAt = LocalDateTime.now(),
            startedAt = null,
            finishedAt = null
        )

        whenever(plagiarismService.startFullCheck(0.8)).thenReturn(summary)

        mockMvc.perform(
            post("/api/plagiarism/check")
                .param("threshold", "0.8")
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        ).andExpect(status().isAccepted)
    }

    @Test
    fun `getRun should return 200 for admin`() {
        val summary = PlagiarismCheckSummaryResponse(
            runId = 2L,
            status = PlagiarismCheckRunStatus.COMPLETED,
            checkedSolutions = 1,
            comparedPairs = 0,
            matches = 0,
            groups = 0,
            threshold = 0.8,
            errorMessage = null,
            createdAt = LocalDateTime.now(),
            startedAt = null,
            finishedAt = null
        )

        whenever(plagiarismService.findRun(2L)).thenReturn(summary)

        mockMvc.perform(
            get("/api/plagiarism/runs/2")
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        ).andExpect(status().isOk)
    }

    @Test
    fun `getCheaterGroups should return 200 for admin`() {
        whenever(plagiarismService.findCheaterGroups()).thenReturn(emptyList())

        mockMvc.perform(
            get("/api/plagiarism/cheaters")
                .requestAttr(
                    "tokenPayload",
                    TokenPayload(userId = 1L, login = "admin", email = "admin@example.com", role = "ADMIN")
                )
        ).andExpect(status().isOk)
    }

}
