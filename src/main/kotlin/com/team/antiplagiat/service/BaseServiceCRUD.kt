package com.team.antiplagiat.service

import com.team.antiplagiat.models.Entity
import io.github.oshai.kotlinlogging.KotlinLogging

abstract class BaseServiceCRUD<T : Entity> : ServiceCRUD<T> {

    protected val logger = KotlinLogging.logger {}

    override val entities: MutableMap<Long, T> = mutableMapOf()

    override fun create(entity: T): Boolean {
        logger.info { "Попытка создания ${entity::class.simpleName} с id=${entity.id}" }
        return if (entities.containsKey(entity.id)) {
            logger.warn { "${entity::class.simpleName} с id=${entity.id} уже существует" }
            false
        } else {
            entities[entity.id] = entity
            logger.info { "${entity::class.simpleName} с id=${entity.id} успешно создан" }
            true
        }
    }

    override fun read(id: Long): T? {
        logger.info { "Поиск ${entities::class.simpleName} с id=$id" }
        val entity = entities[id]
        if (entity == null) {
            logger.warn { "${entities::class.simpleName} с id=$id не найден" }
        } else {
            logger.info { "${entities::class.simpleName} с id=$id найден" }
        }
        return entity
    }

    override fun delete(id: Long): Boolean {
        logger.info { "Удаление ${entities::class.simpleName} с id=$id" }
        return if (entities.containsKey(id)) {
            entities.remove(id)
            logger.info { "${entities::class.simpleName} с id=$id успешно удалён" }
            true
        } else {
            logger.warn { "${entities::class.simpleName} с id=$id не найден, удаление невозможно" }
            false
        }
    }
}