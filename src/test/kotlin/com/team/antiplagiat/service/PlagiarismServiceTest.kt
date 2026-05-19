package com.team.antiplagiat.service

import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.models.PlagiarismMatch
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.PlagiarismCheckRunRepository
import com.team.antiplagiat.repository.PlagiarismMatchRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class PlagiarismServiceTest {

    private lateinit var plagiarismCheckRunRepository: PlagiarismCheckRunRepository
    private lateinit var plagiarismMatchRepository: PlagiarismMatchRepository
    private lateinit var plagiarismCheckRunner: PlagiarismCheckRunner
    private lateinit var plagiarismService: PlagiarismService

    @BeforeEach
    fun setUp() {
        plagiarismCheckRunRepository = mockk()
        plagiarismMatchRepository = mockk()
        plagiarismCheckRunner = mockk()
        plagiarismService = PlagiarismService(
            plagiarismCheckRunRepository,
            plagiarismMatchRepository,
            plagiarismCheckRunner
        )
    }

    @Test
    fun `startFullCheck should create run and delegate execution to async runner`() {
        val savedRun = PlagiarismCheckRun(id = 42L, threshold = 0.75)

        every { plagiarismCheckRunRepository.save(any()) } returns savedRun
        every { plagiarismCheckRunner.run(42L) } just runs

        val result = plagiarismService.startFullCheck(0.75)

        assertEquals(42L, result.runId)
        // startFullCheck returns PlagiarismCheckStartResponse with status as string
        assertEquals(PlagiarismCheckRunStatus.PENDING.name, result.status)
        verify(exactly = 1) { plagiarismCheckRunner.run(42L) }
    }

    @Test
    fun `findCheaterGroups should use latest completed run`() {
        val run = PlagiarismCheckRun(id = 7L, threshold = 0.8, status = PlagiarismCheckRunStatus.COMPLETED)
        val firstSolution = solution(1L, 10L, 100L, "Kotlin", "code1")
        val secondSolution = solution(2L, 11L, 100L, "Kotlin", "code2")
        val matches = listOf(match(run, 1L, firstSolution, secondSolution, 0.95))

        every {
            plagiarismCheckRunRepository.findFirstByStatusOrderByFinishedAtDesc(PlagiarismCheckRunStatus.COMPLETED)
        } returns run
        every { plagiarismMatchRepository.findAllByCheckRun(run) } returns matches

        val groups = plagiarismService.findCheaterGroups()

        assertEquals(1, groups.size)
        assertEquals(listOf(10L, 11L), groups.single().users.map { it.userId })
        assertEquals(listOf(1L, 2L), groups.single().members.map { it.solutionId })
    }

    @Test
    fun `findRun should return saved run summary`() {
        val run = PlagiarismCheckRun(id = 9L, threshold = 0.8, status = PlagiarismCheckRunStatus.COMPLETED)

        every { plagiarismCheckRunRepository.findById(9L) } returns Optional.of(run)

        val result = plagiarismService.findRun(9L)

        assertEquals(9L, result.runId)
        assertEquals(PlagiarismCheckRunStatus.COMPLETED, result.status)
    }

    private fun solution(
        id: Long,
        userId: Long,
        problemId: Long,
        language: String,
        code: String
    ): Solution =
        Solution(
            id = id,
            user = User(id = userId, login = "user$userId", email = "user$userId@example.com"),
            problem = Problem(id = problemId, name = "Problem $problemId"),
            language = language,
            filePath = "/solution$id.kt",
            code = code
        )

    private fun match(
        run: PlagiarismCheckRun,
        id: Long,
        firstSolution: Solution,
        secondSolution: Solution,
        similarity: Double
    ): PlagiarismMatch =
        PlagiarismMatch(
            id = id,
            checkRun = run,
            firstSolution = firstSolution,
            secondSolution = secondSolution,
            similarity = similarity,
            threshold = 0.8
        )
}
