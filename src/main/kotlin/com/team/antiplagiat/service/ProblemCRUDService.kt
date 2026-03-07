package com.team.antiplagiat.service

import com.team.antiplagiat.models.Problem
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class ProblemCrudService : BaseServiceCRUD<Problem>() {

    private val idCounter = AtomicLong(1)

    fun createProblem(name: String, description: String? = null): Boolean {
        logger.info { "Попытка создать задачу: '$name'" }

        val id = idCounter.getAndIncrement()
        val problem = Problem(id, name, description)

        val result = create(problem)

        if (result) {
            logger.info { "Задача создана успешно: ID=$id, '$name'" }
        } else {
            logger.warn { "Не удалось создать задачу: '$name'" }
        }

        return result
    }

    fun getAllProblems(): List<Problem> {
        logger.info { "Получение всех задач. Всего: ${entities.size}" }
        return entities.values.toList()
    }

    fun updateProblem(id: Long, name: String?, description: String? = null): Boolean {
        logger.info { "Попытка обновить задачу ID=$id" }

        val problem = entities[id] ?: return false

        val updatedProblem = problem.copy(
            name = name ?: problem.name,
            description = description ?: problem.description
        )

        entities.remove(id)
        entities[id] = updatedProblem

        logger.info { "Задача обновлена: ID=$id, '${problem.name}' → '$name'" }
        return true
    }

    override fun delete(id: Long): Boolean {
        logger.info { "Попытка удалить задачу ID=$id" }

        val problemName = entities[id]?.name
        val result = super.delete(id)

        if (result) {
            logger.info { "Задача удалена: ID=$id, название='$problemName'" }
        } else {
            logger.warn { "Удаление не удалось: задача с ID=$id не найдена" }
        }

        return result
    }

    fun getProblemCount(): Int = entities.size
}