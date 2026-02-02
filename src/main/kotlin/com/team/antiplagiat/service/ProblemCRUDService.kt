package com.team.antiplagiat.service

import com.team.antiplagiat.ProblemServiceConfig
import com.team.antiplagiat.models.Problem
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicLong

@Service
class ProblemCrudService(
    private val config: ProblemServiceConfig
) {
    private val logger = LoggerFactory.getLogger(ProblemCrudService::class.java)
    private val problems = mutableListOf<Problem>()
    private val idCounter = AtomicLong(1)

    // CREATE - создать новую задачу
    fun createProblem(problem: Problem): Problem {
        // Проверка на запрещенные имена
        // Проверка на лимит задач
        // Генерация ID
        // Добавление задачи в список
        return problem
    }

    // READ - получить все задачи
    fun getAllProblems(): List<Problem> {
        return problems.toList()
    }

    // READ - получить задачу по ID
    fun getProblemById(id: Long): Problem? {
        return problems.find { it.id == id }
    }

    // UPDATE - обновить задачу
    fun updateProblem(id: Long, updatedName: String, updatedDescription: String? = null): Problem? {
        // Поиск задачи по ID
        // Проверка на запрещенные имена
        // Обновление задачи
        return null
    }

    // DELETE - удалить задачу
    fun deleteProblem(id: Long): Boolean {
        // Удаление задачи по ID
        return false
    }

    // GET - получить количество задач
    fun getProblemCount(): Int {
        return problems.size
    }
}