package com.team.antiplagiat.service

import com.team.antiplagiat.config.ContestConfig
import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.ContestRepository
import com.team.antiplagiat.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
@Transactional
class ContestService(
    private val contestRepository: ContestRepository,
    private val userRepository: UserRepository,
    private val config: ContestConfig
) {

    private val logger = KotlinLogging.logger {}

    fun create(contest: Contest): Contest? {
        logger.info { "Создание контеста: ${contest.name}" }
        val maxDurationSeconds = config.maxDurationHours * 3600
        if (contest.duration > maxDurationSeconds) {
            logger.error { "Длительность ${contest.duration} превышает лимит $maxDurationSeconds" }
            return null
        }
        val admin = userRepository.findById(contest.admin.id).orElse(null)
        if (admin == null || admin.role != User.Role.ADMIN) {
            logger.warn { "Администратор с id=${contest.admin.id} не найден или не является admin" }
            return null
        }
        return contestRepository.save(contest)
    }

    fun findById(id: Long): Contest? = contestRepository.findById(id).orElse(null)

    fun findAll(): List<Contest> = contestRepository.findAll()

    fun findByAdmin(adminId: Long): List<Contest> = contestRepository.findAllByAdminId(adminId)

    fun update(id: Long, name: String?, duration: Long?): Contest? {
        val contest = findById(id) ?: return null
        name?.let { contest.name = it }
        duration?.let {
            val maxDurationSeconds = config.maxDurationHours * 3600
            if (it > maxDurationSeconds) {
                logger.error { "Новая длительность $it превышает лимит" }
                return null
            }
            contest.duration = it.toInt()
        }
        return contestRepository.save(contest)
    }

    fun delete(id: Long) {
        logger.info { "Удаление контеста id=$id" }
        contestRepository.deleteById(id)
    }
}