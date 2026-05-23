package com.team.antiplagiat.service

import com.team.antiplagiat.config.ContestConfig
import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.ContestRepository
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.repository.SolutionRepository
import com.team.antiplagiat.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ContestService(
    private val contestRepository: ContestRepository,
    private val userRepository: UserRepository,
    private val problemRepository: ProblemRepository,
    private val solutionRepository: SolutionRepository,
    private val config: ContestConfig,
    private val meterRegistry: MeterRegistry
) {

    private val logger = KotlinLogging.logger {}

    data class OwnedContestProblem(
        val contest: Contest,
        val problem: Problem
    )

    fun create(contest: Contest): Contest? {
        logger.info { "Создание контеста: ${contest.name}" }
        logger.debug { "Параметры: name=${contest.name}, duration=${contest.duration}, adminId=${contest.admin.id}" }

        val maxDurationSeconds = config.maxDurationHours * 3600
        if (contest.duration > maxDurationSeconds) {
            logger.error { "Длительность ${contest.duration} превышает лимит $maxDurationSeconds" }
            logger.debug { "Максимально допустимая: $maxDurationSeconds" }
            meterRegistry.counter("contest.create.failed.duration_limit").increment()
            return null
        }

        val admin = userRepository.findById(contest.admin.id).orElse(null)
        if (admin == null) {
            logger.warn { "Пользователь id=${contest.admin.id} не найден" }
            meterRegistry.counter("contest.create.failed.invalid_user").increment()
            return null
        }
        contest.admin = admin

        return contestRepository.save(contest).also {
            logger.info { "Контест создан: id=${it.id}, name=${it.name}" }
            logger.debug { "Saved: $it" }
            meterRegistry.counter("contest.created").increment()
        }
    }

    fun findById(id: Long): Contest? {
        logger.debug { "Поиск контеста id=$id" }
        return contestRepository.findById(id).orElse(null).also {
            if (it != null) {
                logger.debug { "Найден: $it" }
            } else {
                logger.debug { "Не найден" }
            }
        }
    }

    fun findAll(): List<Contest> {
        logger.debug { "Получение всех контестов" }
        return contestRepository.findAll().also {
            logger.debug { "Найдено: ${it.size}" }
        }
    }

    fun findByAdmin(adminId: Long): List<Contest> {
        logger.debug { "Поиск контестов администратора id=$adminId" }
        return contestRepository.findAllByAdminId(adminId).also {
            logger.debug { "Найдено: ${it.size} контестов" }
        }
    }

    fun findByIdAndOwner(id: Long, ownerId: Long): Contest? {
        logger.debug { "Поиск контеста id=$id владельца id=$ownerId" }
        val contest = findById(id) ?: return null
        return contest.takeIf { it.admin.id == ownerId }
    }

    fun createProblemInContest(
        contestId: Long,
        ownerId: Long,
        name: String,
        description: String?,
        condition: String?
    ): Problem {
        val contest = findByIdAndOwner(contestId, ownerId)
            ?: throw ResourceNotFoundException("Contest with id=$contestId not found")

        val problem = problemRepository.save(
            Problem(name = name, description = description, condition = condition)
        )
        contest.problems.add(problem)
        contestRepository.save(contest)
        meterRegistry.counter("contest.problem.created").increment()
        return problem
    }

    fun addProblem(contestId: Long, ownerId: Long, problemId: Long): Problem {
        val contest = findByIdAndOwner(contestId, ownerId)
            ?: throw ResourceNotFoundException("Contest with id=$contestId not found")
        val problem = problemRepository.findById(problemId)
            .orElseThrow { ResourceNotFoundException("Problem with id=$problemId not found") }

        contest.problems.add(problem)
        contestRepository.save(contest)
        meterRegistry.counter("contest.problem.added").increment()
        return problem
    }

    fun hasProblem(contestId: Long, ownerId: Long, problemId: Long): Boolean =
        findByIdAndOwner(contestId, ownerId)
            ?.problems
            ?.any { it.id == problemId }
            ?: false

    fun findOwnedProblem(contestId: Long, ownerId: Long, problemId: Long): OwnedContestProblem? {
        val contest = findByIdAndOwner(contestId, ownerId) ?: return null
        val problem = contest.problems.firstOrNull { it.id == problemId } ?: return null
        return OwnedContestProblem(contest, problem)
    }

    fun updateProblem(contestId: Long, ownerId: Long, problemId: Long, name: String?, description: String?, condition: String?): Problem {
        val contestProblem = findOwnedProblem(contestId, ownerId, problemId)
            ?: throw ResourceNotFoundException("Problem with id=$problemId not found in contest $contestId")

        name?.let { contestProblem.problem.name = it }
        description?.let { contestProblem.problem.description = it }
        condition?.let { contestProblem.problem.condition = it }

        return problemRepository.save(contestProblem.problem)
    }

    fun deleteProblem(contestId: Long, ownerId: Long, problemId: Long) {
        val contestProblem = findOwnedProblem(contestId, ownerId, problemId)
            ?: throw ResourceNotFoundException("Problem with id=$problemId not found in contest $contestId")

        solutionRepository.deleteAllByContestAndProblem(contestProblem.contest, contestProblem.problem)
        solutionRepository.deleteAllByProblem(contestProblem.problem)
        contestProblem.contest.problems.removeIf { it.id == problemId }
        contestRepository.saveAndFlush(contestProblem.contest)
        problemRepository.delete(contestProblem.problem)
        meterRegistry.counter("contest.problem.deleted").increment()
    }

    fun update(id: Long, name: String?, duration: Long?): Contest? {
        logger.info { "Обновление контеста id=$id: name=$name, duration=$duration" }
        logger.debug { "Параметры: name=$name, duration=$duration" }

        val contest = findById(id) ?: run {
            logger.warn { "Контест id=$id не найден" }
            meterRegistry.counter("contest.update.failed.not_found").increment()
            return null
        }

        name?.let {
            logger.debug { "Изменение имени: '${contest.name}' -> '$it'" }
            contest.name = it
        }

        duration?.let {
            val maxDurationSeconds = config.maxDurationHours * 3600
            if (it > maxDurationSeconds) {
                logger.error { "Новая длительность $it превышает лимит" }
                logger.debug { "Максимально допустимая: $maxDurationSeconds" }
                meterRegistry.counter("contest.update.failed.duration_limit").increment()
                return null
            }
            logger.debug { "Изменение длительности: ${contest.duration} -> $it" }
            contest.duration = it.toInt()
        }

        return contestRepository.save(contest).also {
            logger.info { "Контест обновлен: id=${it.id}" }
            logger.debug { "Saved: $it" }
            meterRegistry.counter("contest.updated").increment()
        }
    }

     fun delete(id: Long) {
         logger.info { "Удаление контеста id=$id" }
         logger.debug { "Начало проверки существования контеста" }

         if (!contestRepository.existsById(id)) {
             logger.warn { "Попытка удаления несуществующего контеста: id=$id" }
             logger.debug { "Contest с id=$id не найден в базе данных" }
             meterRegistry.counter("contest.deleted.failed.not_found").increment()
             throw ResourceNotFoundException("Contest with id=$id not found")
         }

         val contest = contestRepository.findById(id)
             .orElseThrow { ResourceNotFoundException("Contest with id=$id not found") }
         val problems = contest.problems.toList()

         solutionRepository.deleteAllByContest(contest)
         problems.forEach { solutionRepository.deleteAllByProblem(it) }
         contest.problems.clear()
         contestRepository.saveAndFlush(contest)
         problems.forEach { problemRepository.delete(it) }
         contestRepository.delete(contest)
         logger.info { "Контест удален: id=$id" }
         meterRegistry.counter("contest.deleted").increment()
     }
}
