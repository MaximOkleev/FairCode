package com.team.antiplagiat.service

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.exception.TooManyAttemptsException
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.repository.ContestRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import com.team.antiplagiat.config.props.SolutionConfig
import com.team.antiplagiat.models.SolutionStatus
import java.time.LocalDateTime


@Service
class SolutionService(
    private val solutionRepository: SolutionRepository,
    private val userRepository: UserRepository,
    private val problemRepository: ProblemRepository,
    private val contestRepository: ContestRepository,
    private val properties: SolutionConfig,
    private val meterRegistry: MeterRegistry
) {

    private val logger = KotlinLogging.logger {}

    @Transactional
    fun create(userId: Long, problemId: Long, language: String, filePath: String, code: String?): Solution {
        logger.info { "Попытка создать решение: userId=$userId, problemId=$problemId, language=$language" }
        logger.debug { "Параметры: userId=$userId, problemId=$problemId, language='$language', filePath='$filePath', code='${code?.take(50)}...'" }

        val user = userRepository.findById(userId)
            .orElseThrow { 
                logger.warn { "Пользователь не найден: userId=$userId" }
                ResourceNotFoundException("Пользователь с id=$userId не найден") 
            }
        logger.debug { "Пользователь найден: id=${user.id}, login=${user.login}" }

        val problem = problemRepository.findById(problemId)
            .orElseThrow { 
                logger.warn { "Задача не найдена: problemId=$problemId" }
                ResourceNotFoundException("Задача с id=$problemId не найдена") 
            }
        logger.debug { "Задача найдена: id=${problem.id}, name=${problem.name}" }

        val attempts = solutionRepository.countByUserAndProblem(user, problem)
        logger.debug { "Количество попыток пользователя: $attempts из ${properties.maxAttempts} максимально" }
        
        if (attempts >= properties.maxAttempts) {
            logger.warn { "Превышен лимит попыток ${properties.maxAttempts} для userId=${user.id}, problemId=${problem.id}" }
            meterRegistry.counter("solution.created.failed.too_many_attempts.total").increment()
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
        logger.debug { "Объект Solution создан: status=${solution.status}, submittedAt=${solution.submittedAt}" }

        return solutionRepository.save(solution).also {
            logger.info { "Решение успешно создано: id=${it.id}, userId=$userId, problemId=$problemId, status=${it.status}" }
            logger.debug { "Сохранённое решение: $it" }
            meterRegistry.counter("solution.created.total").increment()
        }
    }

    @Transactional
    fun createInContest(
        ownerId: Long,
        contestId: Long,
        problemId: Long,
        language: String,
        filePath: String,
        code: String?
    ): Solution {
        logger.info { "Создание решения в контесте: ownerId=$ownerId, contestId=$contestId, problemId=$problemId" }

        val user = userRepository.findById(ownerId)
            .orElseThrow { ResourceNotFoundException("Пользователь с id=$ownerId не найден") }
        val contest = contestRepository.findById(contestId)
            .orElseThrow { ResourceNotFoundException("Контест с id=$contestId не найден") }
        if (contest.admin.id != ownerId) {
            throw ResourceNotFoundException("Контест с id=$contestId не найден")
        }

        val problem = contest.problems.firstOrNull { it.id == problemId }
            ?: throw ResourceNotFoundException("Задача с id=$problemId не найдена в контесте $contestId")

        val attempts = solutionRepository.countByUserAndContestAndProblem(user, contest, problem)
        if (attempts >= properties.maxAttempts) {
            meterRegistry.counter("solution.created.failed.too_many_attempts.total").increment()
            throw TooManyAttemptsException("Превышен лимит попыток: ${properties.maxAttempts}")
        }

        if (solutionRepository.existsByUserAndContestAndProblemAndFilePath(user, contest, problem, filePath)) {
            throw IllegalArgumentException("Решение с таким путем файла уже добавлено в этот контест")
        }

        val solution = Solution(
            user = user,
            contest = contest,
            problem = problem,
            language = language,
            status = SolutionStatus.WAITING,
            submittedAt = LocalDateTime.now(),
            filePath = filePath,
            code = code
        )

        return solutionRepository.save(solution).also {
            meterRegistry.counter("solution.created.total").increment()
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Solution {
        logger.debug { "Поиск решения по id=$id" }
        return solutionRepository.findById(id)
            .also { solution ->
                if (solution.isPresent) {
                    logger.debug { "Решение найдено: id=$id, status=${solution.get().status}" }
                } else {
                    logger.debug { "Решение не найдено: id=$id" }
                }
            }
            .orElseThrow { 
                ResourceNotFoundException("Решение с id=$id не найдено") 
            }
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Solution> {
        logger.debug { "Получение всех решений" }
        val solutions = solutionRepository.findAll()
        logger.debug { "Найдено решений: ${solutions.size}" }
        return solutions
    }

    @Transactional(readOnly = true)
    fun findByUser(userId: Long): List<Solution> {
        logger.debug { "Поиск решений пользователя: userId=$userId" }
        val solutions = solutionRepository.findAllByUserId(userId)
        logger.debug { "Найдено решений для пользователя $userId: ${solutions.size}" }
        return solutions
    }

    @Transactional(readOnly = true)
    fun findByUserAndContest(userId: Long, contestId: Long): List<Solution> =
        solutionRepository.findAllByUserIdAndContestId(userId, contestId)

    @Transactional(readOnly = true)
    fun findOwnedWithRelations(solutionId: Long, ownerId: Long): Solution {
        val solution = solutionRepository.findByIdWithRelations(solutionId)
            ?: throw ResourceNotFoundException("Решение с id=$solutionId не найдено")
        if (solution.user.id != ownerId) {
            throw ResourceNotFoundException("Решение с id=$solutionId не найдено")
        }
        return solution
    }

    @Transactional(readOnly = true)
    fun findOwnedInContestProblem(ownerId: Long, contestId: Long, problemId: Long, solutionId: Long): Solution {
        val solution = findOwnedWithRelations(solutionId, ownerId)
        if (solution.contest?.id != contestId || solution.problem.id != problemId) {
            throw ResourceNotFoundException("Решение с id=$solutionId не найдено")
        }
        return solution
    }

    @Transactional
    fun updateInContestProblem(
        ownerId: Long,
        contestId: Long,
        problemId: Long,
        solutionId: Long,
        language: String,
        filePath: String,
        code: String
    ): Solution {
        val solution = findOwnedInContestProblem(ownerId, contestId, problemId, solutionId)
        val contest = solution.contest ?: throw ResourceNotFoundException("Контест с id=$contestId не найден")

        if (solution.filePath != filePath &&
            solutionRepository.existsByUserAndContestAndProblemAndFilePath(solution.user, contest, solution.problem, filePath)
        ) {
            throw IllegalArgumentException("Решение с таким путем файла уже добавлено в этот контест")
        }

        solution.language = language
        solution.filePath = filePath
        solution.code = code
        return solutionRepository.save(solution)
    }

    @Transactional
    fun updateOwned(
        ownerId: Long,
        solutionId: Long,
        language: String,
        filePath: String,
        code: String
    ): Solution {
        val solution = findOwnedWithRelations(solutionId, ownerId)
        val contest = solution.contest

        if (solution.filePath != filePath) {
            val duplicateExists = if (contest != null) {
                solutionRepository.existsByUserAndContestAndProblemAndFilePath(
                    solution.user,
                    contest,
                    solution.problem,
                    filePath
                )
            } else {
                solutionRepository.existsByUserAndProblemAndFilePath(solution.user, solution.problem, filePath)
            }

            if (duplicateExists) {
                throw IllegalArgumentException("Решение с таким путем файла уже добавлено")
            }
        }

        solution.language = language
        solution.filePath = filePath
        solution.code = code
        return solutionRepository.save(solution)
    }

    @Transactional
    fun deleteInContestProblem(ownerId: Long, contestId: Long, problemId: Long, solutionId: Long) {
        val solution = findOwnedInContestProblem(ownerId, contestId, problemId, solutionId)
        solutionRepository.delete(solution)
        meterRegistry.counter("solution.deleted.total").increment()
    }

    @Transactional
    fun updateStatus(id: Long, status: SolutionStatus): Solution {
        logger.info { "Обновление статуса решения: id=$id, newStatus=$status" }
        logger.debug { "Операция обновления статуса: id=$id, status=$status" }
        
        val solution = findById(id)
        val oldStatus = solution.status
        logger.debug { "Старый статус: $oldStatus, новый статус: $status" }
        
        solution.status = status
        return solutionRepository.save(solution).also {
            logger.info { "Статус решения обновлен: id=${it.id}, oldStatus=$oldStatus, newStatus=${it.status}" }
            logger.debug { "Сохранённое решение: $it" }
            meterRegistry.counter("solution.status.updated.total").increment()
            meterRegistry.counter("solution.status.$status.total").increment()
        }
    }

    @Transactional
    fun delete(id: Long) {
        logger.info { "Начало удаления решения: id=$id" }
        logger.debug { "Выполняется DELETE операция для решения id=$id" }
        
        return try {
            solutionRepository.deleteById(id)
            logger.info { "Решение успешно удалено: id=$id" }
            logger.debug { "Операция DELETE завершена успешно для решения id=$id" }
            meterRegistry.counter("solution.deleted.total").increment()
        } catch (e: Exception) {
            logger.error { "Ошибка при удалении решения id=$id: ${e.message}" }
            logger.debug { "Stack trace: ${e.stackTrace.joinToString("\n")}" }
            meterRegistry.counter("solution.deleted.failed.total").increment()
            throw e
        }
    }
}
