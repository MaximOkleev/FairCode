package com.team.antiplagiat.service

import com.team.antiplagiat.models.AiGeneratedSolution
import com.team.antiplagiat.models.AiGeneratedSolutionStatus
import com.team.antiplagiat.models.AiPlagiarismCheckRun
import com.team.antiplagiat.models.AiProvider
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.AiGeneratedSolutionRepository
import com.team.antiplagiat.repository.AiPlagiarismCheckRunRepository
import com.team.antiplagiat.repository.AiPlagiarismMatchRepository
import com.team.antiplagiat.repository.ContestRepository
import com.team.antiplagiat.repository.SolutionRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AiPlagiarismServiceTest {

    private lateinit var solutionRepository: SolutionRepository
    private lateinit var contestRepository: ContestRepository
    private lateinit var aiGeneratedSolutionRepository: AiGeneratedSolutionRepository
    private lateinit var aiGeneratedSolutionService: AiGeneratedSolutionService
    private lateinit var aiPlagiarismCheckRunRepository: AiPlagiarismCheckRunRepository
    private lateinit var aiPlagiarismMatchRepository: AiPlagiarismMatchRepository
    private lateinit var aiPlagiarismService: AiPlagiarismService

    @BeforeEach
    fun setUp() {
        solutionRepository = mockk()
        contestRepository = mockk()
        aiGeneratedSolutionRepository = mockk()
        aiGeneratedSolutionService = mockk()
        aiPlagiarismCheckRunRepository = mockk()
        aiPlagiarismMatchRepository = mockk(relaxed = true)
        aiPlagiarismService = AiPlagiarismService(
            solutionRepository,
            contestRepository,
            aiGeneratedSolutionRepository,
            aiGeneratedSolutionService,
            aiPlagiarismCheckRunRepository,
            aiPlagiarismMatchRepository
        )
    }

    @Test
    fun `checkSolution should compare with all successful ai solutions for same problem and language`() {
        val problem = Problem(id = 10L, name = "A+B")
        val solution = Solution(
            id = 1L,
            user = User(id = 5L, login = "owner", email = "owner@example.com"),
            problem = problem,
            language = "Kotlin",
            filePath = "Main.kt",
            code = "fun sum(a: Int, b: Int): Int = a + b"
        )
        val aiSolutions = listOf(
            aiSolution(101L, problem, AiProvider.CHATGPT, "fun add(x: Int, y: Int): Int = x + y"),
            aiSolution(102L, problem, AiProvider.GEMINI, "fun multiply(x: Int, y: Int): Int = x * y"),
            aiSolution(103L, problem, AiProvider.DEEPSEEK, "fun plus(first: Int, second: Int): Int = first + second")
        )

        every { solutionRepository.findByIdWithRelations(1L) } returns solution
        every {
            aiGeneratedSolutionRepository.findComparableByProblemAndLanguage(
                problemId = 10L,
                language = "Kotlin"
            )
        } returns aiSolutions
        every { aiPlagiarismCheckRunRepository.save(any()) } answers {
            firstArg<AiPlagiarismCheckRun>().also { it.id = 77L }
        }

        val result = aiPlagiarismService.checkSolution(ownerId = 5L, solutionId = 1L, threshold = 0.8)

        assertEquals(77L, result.runId)
        assertEquals(1, result.checkedSolutions)
        assertEquals(3, result.comparedPairs)
        assertEquals(
            setOf(AiProvider.CHATGPT, AiProvider.GEMINI, AiProvider.DEEPSEEK),
            result.pairs.map { it.provider }.toSet()
        )
        assertTrue(result.pairs.any { !it.plagiarism })
    }

    private fun aiSolution(
        id: Long,
        problem: Problem,
        provider: AiProvider,
        code: String
    ): AiGeneratedSolution =
        AiGeneratedSolution(
            id = id,
            problem = problem,
            provider = provider,
            language = "Kotlin",
            modelName = provider.name.lowercase(),
            status = AiGeneratedSolutionStatus.SUCCESS,
            prompt = "prompt",
            code = code
        )
}
