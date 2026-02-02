package com.team.antiplagiat.service

import com.team.antiplagiat.models.Entity
import com.team.antiplagiat.models.Solution

class SolutionService(
    private val properties: AntiPlagiatProperties
) {
    private val entities = mutableMapOf<Long, Entity>()
    private val attemptsCounter = mutableMapOf<Pair<Long, Long>, Int>()

    fun create(entity: Entity): Boolean {
        println("Попытка создать сущность: $entity")

        if (entity is Solution) {
            val key = entity.userId to entity.taskId
            val attempts = attemptsCounter.getOrDefault(key, 0)

            if (attempts >= properties.maxAttempts) {
                println(
                    "Превышено кол-во попыток ${properties.maxAttempts} для userId ${entity.userId}, taskId ${entity.taskId}"
                )
                return false
            }

            entities[entity.id] = entity
            attemptsCounter[key] = attempts + 1
            println(
                "Сущность создана. id ${entity.id}, попытка ${attemptsCounter[key]}/${properties.maxAttempts}"
            )
            return true
        }

        entities[entity.id] = entity
        println("Сущность создана (generic). id ${entity.id}")
        return true
    }

    fun read(id: Long): Entity? {
        val entity = entities[id]
        println("Чтение сущности id $id → $entity")
        return entity
    }

    fun update(id: Long, updated: Entity): Boolean {
        if (!entities.containsKey(id)) {
            println("Обновление невозможно, т.к. сущность id $id не найдена")
            return false
        }

        entities[id] = updated
        println("Сущность обновлена, id $id")
        return true
    }

    fun delete(id: Long): Boolean {
        val removed = entities.remove(id)
        if (removed == null) {
            println("Сущность $id не найдена, невозможно удалить")
            return false
        }

        println("Сущность $id удалена")
        return true
    }
}
