package com.team.antiplagiat.service

import com.team.antiplagiat.models.Solution
import org.springframework.stereotype.Service
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class SolutionService(
    private val properties: AntiPlagiatProperties
) : ServiceCRUD<Solution> {

    private val logger = KotlinLogging.logger {}

    override val entities = mutableMapOf<Long, Solution>()

    private val attemptsCounter = mutableMapOf<Pair<Long, Long>, Int>()

    override fun create(entity: Solution): Boolean {
        logger.info { "Попытка создать посылку: $entity" }

        val key = entity.userId to entity.taskId
        val attempts = attemptsCounter.getOrDefault(key, 0)

        if (attempts >= properties.maxAttempts) {
            logger.warn { "Превышено кол-во попыток ${properties.maxAttempts} для userId ${entity.userId}, taskId ${entity.taskId}" }
            return false
        }

        val created = super.create(entity)
        if (created) {
            attemptsCounter[key] = attempts + 1
            logger.info { "Посылка создана. id ${entity.id}, попытка ${attemptsCounter[key]}/${properties.maxAttempts}" }
        } else {
            logger.warn { "Посылка с id ${entity.id} уже существует" }
        }

        return created
    }

    fun update(id: Long, updated: Solution): Boolean {
        if (!entities.containsKey(id)) {
            logger.warn { "Обновление невозможно, т.к. посылка id $id не найдена" }
            return false
        }

        entities[id] = updated
        logger.info { "Посылка обновлена, id $id" }
        return true
    }
}