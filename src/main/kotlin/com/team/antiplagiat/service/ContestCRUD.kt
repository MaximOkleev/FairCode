package com.team.antiplagiat.service

import com.team.antiplagiat.models.Contest
import org.springframework.stereotype.Service
import com.team.antiplagiat.ContestConfig
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class ContestCRUD(private val config: ContestConfig) : ServiceCRUD<Contest> {

    private val logger = KotlinLogging.logger {}

    override val entities: MutableMap<Long, Contest> = mutableMapOf()

    override fun create(entity: Contest): Boolean {
        logger.info { "Попытка создать контест: id=${entity.id}, name='${entity.name}'" }
        val maxDurationSeconds = config.maxDurationHours * 3600
        if (entity.duration > maxDurationSeconds) {
            logger.error { "Длительность контеста слишком большая!" }
            logger.error { "Текущая: ${entity.duration} сек. (${entity.duration / 3600} часов)" }
            logger.error { "Максимально разрешенная: $maxDurationSeconds сек. (${config.maxDurationHours} часов)" }
            return false
        }
        if (entities.containsKey(entity.id)) {
            logger.warn { "Конкурс с id=${entity.id} уже существует" }
            return false
        }

        entities[entity.id] = entity
        logger.info { "Контест создан: id=${entity.id}" }
        return true
    }

    fun update(id: Long, name: String?, duration: Long?): Boolean {
        logger.info { "Обновление контест id=$id, name='$name', duration=$duration" }
        val contest = entities[id]
        if (contest == null) {
            logger.warn { "Конкурс id=$id не найден" }
            return false
        }
        logger.info { "До: name='${contest.name}', duration=${contest.duration}" }
        if (duration != null) {
            val maxDurationSeconds = config.maxDurationHours * 3600
            if (duration > maxDurationSeconds) {
                logger.error { "Новая длительность слишком большая!" }
                logger.error { "Запрошено: $duration сек. (${duration / 3600} часов)" }
                logger.error { "Максимально разрешенная: $maxDurationSeconds сек. (${config.maxDurationHours} часов)" }
                return false
            }
        }
        if (name != null) contest.name = name
        if (duration != null) contest.duration = duration
        logger.info { "После: name='${contest.name}', duration=${contest.duration}" }
        return true
    }

    override fun read(id: Long): Contest? {
        logger.info { "Поиск контеста с id=$id" }
        val contest = entities[id]
        if (contest == null) {
            logger.warn { "Контест с id=$id не найден" }
            logger.debug { "Доступные ID контестов: ${entities.keys.sorted().joinToString()}" }
            return null
        } else {
            logger.info { "Контест найден: id=${contest.id}, name='${contest.name}'" }
            logger.debug { "Подробности контеста: $contest" }
            return contest
        }
    }

    fun getByAdmin(adminId: Long): List<Contest>? {
        logger.info { "Поиск контестов администратора adminId=$adminId" }
        val adminContests = entities.values.filter { it.adminId == adminId }.sortedByDescending { it.startedAt }.toList()
        if (adminContests.isEmpty()) {
            logger.warn { "У администратора adminId=$adminId нет контестов" }
            logger.debug { "Всего контестов в системе: ${entities.size}" }
            return null
        } else {
            logger.info { "Найдено ${adminContests.size} контестов для adminId=$adminId" }
            adminContests.forEachIndexed { index, contest ->
                logger.info { "Контест ${index + 1}: id=${contest.id}, name='${contest.name}', started=${contest.startedAt}, duration=${contest.duration}" }
            }
            return adminContests
        }
    }

    override fun delete(id: Long): Boolean {
        logger.info { "Удаление контеста id=$id" }
        if (entities.remove(id) == null) {
            logger.warn { "Контест id=$id не найден для удаления" }
            return false
        }
        logger.info { "Контест id=$id удален" }
        return true
    }
}