package com.team.antiplagiat.service

import com.team.antiplagiat.models.PlagiarismCheckRun
import com.team.antiplagiat.models.PlagiarismCheckRunStatus
import com.team.antiplagiat.models.PlagiarismMatch
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.PlagiarismCheckRunRepository
import com.team.antiplagiat.repository.PlagiarismMatchRepository
import com.team.antiplagiat.repository.SolutionBucket
import com.team.antiplagiat.repository.SolutionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class PlagiarismCheckRunnerTest {

    private lateinit var solutionRepository: SolutionRepository
    private lateinit var plagiarismCheckRunRepository: PlagiarismCheckRunRepository
    private lateinit var plagiarismMatchRepository: PlagiarismMatchRepository
    private lateinit var runner: PlagiarismCheckRunner

    @BeforeEach
    fun setUp() {
        solutionRepository = mockk()
        plagiarismCheckRunRepository = mockk()
        plagiarismMatchRepository = mockk()
        runner = PlagiarismCheckRunner(
            solutionRepository,
            plagiarismCheckRunRepository,
            plagiarismMatchRepository
        )
    }

    @Test
    fun `run should skip pairs from the same user and save matches with run id`() {
        val run = PlagiarismCheckRun(id = 5L, threshold = 0.8)
        val sameUserOriginal = solution(1L, 10L, 100L, "Kotlin", "fun sum(a: Int, b: Int) = a + b")
        val sameUserDuplicate = solution(2L, 10L, 100L, "Kotlin", "fun add(x: Int, y: Int) = x + y")
        val anotherUserCopy = solution(3L, 11L, 100L, "Kotlin", "fun plus(x: Int, y: Int) = x + y")
        val savedMatches = slot<Iterable<PlagiarismMatch>>()

        every { plagiarismCheckRunRepository.findById(5L) } returns Optional.of(run)
        every { plagiarismCheckRunRepository.saveAndFlush(run) } returns run
        every { plagiarismCheckRunRepository.save(run) } returns run
        every { solutionRepository.findBucketsWithCode() } returns listOf(bucket(100L, "kotlin"))
        every { solutionRepository.findAllWithCodeByProblemAndLanguage(100L, "kotlin") } returns listOf(
            sameUserOriginal,
            sameUserDuplicate,
            anotherUserCopy
        )
        every { plagiarismMatchRepository.saveAll(capture(savedMatches)) } answers {
            savedMatches.captured.mapIndexed { index, match -> match.apply { id = index + 1L } }
        }

        runner.run(5L)

        assertEquals(PlagiarismCheckRunStatus.COMPLETED, run.status)
        assertEquals(3, run.checkedSolutions)
        assertEquals(2, run.comparedPairs)
        assertEquals(2, run.matches)
        assertTrue(savedMatches.captured.all { it.checkRun.id == 5L })
        assertTrue(savedMatches.captured.none {
            it.firstSolution.user.id == it.secondSolution.user.id
        })
    }

    private fun bucket(problemId: Long, language: String): SolutionBucket =
        object : SolutionBucket {
            override val problemId: Long = problemId
            override val language: String = language
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
}
