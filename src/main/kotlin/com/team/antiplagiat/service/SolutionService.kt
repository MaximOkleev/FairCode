package com.team.antiplagiat.service

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.TooManyAttemptsException
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.repository.ProblemRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import io.github.oshai.kotlinlogging.KotlinLogging
import com.team.antiplagiat.config.props.SolutionConfig
import com.team.antiplagiat.models.SolutionStatus
import java.time.LocalDateTime


@Service
class SolutionService(
    private val solutionRepository: SolutionRepository,
    private val userRepository: UserRepository,
    private val problemRepository: ProblemRepository,
    private val properties: SolutionConfig
) {

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun create(userId: Long, problemId: Long, language: String, filePath: String, code: String?): Solution {
        logger.info { "Попытка создать решение: userId=$userId, problemId=$problemId" }

        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("Пользователь с id=$userId не найден") }

        val problem = problemRepository.findById(problemId)
            .orElseThrow { ResourceNotFoundException("Задача с id=$problemId не найдена") }

        val attempts = solutionRepository.countByUserAndProblem(user, problem)
        if (attempts >= properties.maxAttempts) {
            logger.warn { "Превышен лимит попыток ${properties.maxAttempts} для user=${user.id}, problem=${problem.id}" }
            throw TooManyAttemptsException("Превышен лимит попыток: ${properties.maxAttempts}")
        }

        val solution = Solution(
            user = user,
            problem = problem,
            language = language,
            status = SolutionStatus.WAITING,
            submittedAt = LocalDateTime.now(),
            filePath = filePath,
            code = code
        )

        return solutionRepository.save(solution).also {
            logger.info { "Решение создано: id=${it.id}" }
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Solution {
        return solutionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Решение с id=$id не найдено") }
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Solution> = solutionRepository.findAll()

    @Transactional(readOnly = true)
    fun findByUser(userId: Long): List<Solution> = solutionRepository.findAllByUserId(userId)

    @Transactional
    fun updateStatus(id: Long, status: SolutionStatus): Solution {
        val solution = findById(id)
        solution.status = status
        return solutionRepository.save(solution)
    }

    @Transactional
    fun delete(id: Long) {
        logger.info { "Удаление решения id=$id" }
        if (!solutionRepository.existsById(id)) {
            throw ResourceNotFoundException("Решение с id=$id не найдено")
        }
        solutionRepository.deleteById(id)
    }
}