package com.team.antiplagiat.service

import com.team.antiplagiat.config.props.AiGenerationProperties
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.AiGeneratedSolution
import com.team.antiplagiat.models.AiGeneratedSolutionStatus
import com.team.antiplagiat.models.AiProvider
import com.team.antiplagiat.repository.AiGeneratedSolutionRepository
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.service.ai.OpenRouterClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AiGeneratedSolutionService(
    private val problemRepository: ProblemRepository,
    private val aiGeneratedSolutionRepository: AiGeneratedSolutionRepository,
    private val openRouterClient: OpenRouterClient,
    private val properties: AiGenerationProperties,
    private val meterRegistry: MeterRegistry
) {
    private val logger = KotlinLogging.logger {}

    @Transactional
    fun generateForProblem(problemId: Long, language: String): List<AiGeneratedSolution> {
        val problem = problemRepository.findById(problemId)
            .orElseThrow { ResourceNotFoundException("Задача с id=$problemId не найдена") }

        val prompt = buildPrompt(problem.name, problem.description, problem.condition, language)
        val targets = listOf(
            AiProvider.CHATGPT to properties.models.chatgpt,
            AiProvider.GEMINI to properties.models.gemini,
            AiProvider.DEEPSEEK to properties.models.deepseek
        )

        return targets.map { (provider, modelName) ->
            val generatedSolution = try {
                logger.info { "Генерация AI-решения через OpenRouter: problemId=$problemId, provider=$provider, model=$modelName" }
                val result = openRouterClient.generate(modelName, prompt)
                AiGeneratedSolution(
                    problem = problem,
                    provider = provider,
                    language = language,
                    modelName = result.modelName,
                    status = AiGeneratedSolutionStatus.SUCCESS,
                    prompt = prompt,
                    code = result.code
                )
            } catch (e: Exception) {
                logger.warn(e) { "Не удалось сгенерировать AI-решение через OpenRouter: problemId=$problemId, provider=$provider" }
                AiGeneratedSolution(
                    problem = problem,
                    provider = provider,
                    language = language,
                    modelName = modelName,
                    status = AiGeneratedSolutionStatus.FAILED,
                    prompt = prompt,
                    errorMessage = e.message
                )
            }

            aiGeneratedSolutionRepository.save(generatedSolution).also {
                meterRegistry.counter(
                    "ai.generated.solution.total",
                    "provider", provider.name,
                    "status", it.status.name
                ).increment()
            }
        }
    }

    @Transactional(readOnly = true)
    fun findByProblem(problemId: Long): List<AiGeneratedSolution> {
        if (!problemRepository.existsById(problemId)) {
            throw ResourceNotFoundException("Задача с id=$problemId не найдена")
        }
        return aiGeneratedSolutionRepository.findAllByProblemIdOrderByGeneratedAtDesc(problemId)
    }

    private fun buildPrompt(name: String, description: String?, condition: String?, language: String): String {
        val problemText = listOfNotNull(
            condition?.takeIf { it.isNotBlank() },
            description?.takeIf { it.isNotBlank() }
        ).joinToString("\n\n")

        require(problemText.isNotBlank()) {
            "У задачи должно быть заполнено условие или описание для генерации AI-решения"
        }

        return """
            Write a complete accepted solution for the programming problem.
            Problem name: $name
            Target language: $language

            Problem statement:
            $problemText

            Return source code only, without Markdown fences, comments about the task, or explanations.
        """.trimIndent()
    }
}
