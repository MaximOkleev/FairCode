package com.team.antiplagiat.service

import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.ProblemRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Сервис для управления задачами (Problem).
 *
 * Предоставляет CRUD-операции над сущностью [Problem].
 * Все методы выполняются в рамках транзакции благодаря аннотации [@Transactional].
 *
 * @property problemRepository репозиторий для работы с таблицей `problems`
 */
@Service
@Transactional
class ProblemService(private val problemRepository: ProblemRepository) {

    private val logger = KotlinLogging.logger {}

    /**
     * Создаёт новую задачу и сохраняет её в базе данных.
     *
     * @param name название задачи (обязательное поле)
     * @param description описание задачи (опциональное, может быть `null`)
     * @return созданная задача с присвоенным идентификатором
     */
    fun create(name: String, description: String?): Problem {
        logger.info { "Создание задачи: $name" }
        return problemRepository.save(Problem(name = name, description = description))
    }

    /**
     * Находит задачу по её идентификатору.
     *
     * @param id идентификатор задачи
     * @return задача если найдена, иначе `null`
     */
    fun findById(id: Long): Problem? = problemRepository.findById(id).orElse(null)

    /**
     * Возвращает список всех задач из базы данных.
     *
     * @return список всех задач, пустой список если задач нет
     */
    fun findAll(): List<Problem> = problemRepository.findAll()

    /**
     * Обновляет поля существующей задачи.
     *
     * Обновляются только те поля, которые переданы не `null`.
     * Если задача с указанным [id] не найдена — возвращает `null`.
     *
     * @param id идентификатор задачи для обновления
     * @param name новое название задачи, если `null` — название не изменяется
     * @param description новое описание задачи, если `null` — описание не изменяется
     * @return обновлённая задача, или `null` если задача не найдена
     */
    fun update(id: Long, name: String?, description: String?): Problem? {
        val problem = findById(id) ?: return null
        name?.let { problem.name = it }
        description?.let { problem.description = it }
        return problemRepository.save(problem)
    }

    /**
     * Удаляет задачу по её идентификатору.
     *
     * Если задача с указанным [id] не существует — метод завершается без ошибки.
     *
     * @param id идентификатор задачи для удаления
     */
    fun delete(id: Long) {
        logger.info { "Удаление задачи id=$id" }
        problemRepository.deleteById(id)
    }
}