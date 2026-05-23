package com.team.antiplagiat.service

import com.team.antiplagiat.exception.ResourceNotFoundException
import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.ProblemRepository
import com.team.antiplagiat.repository.SolutionRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Сервис для управления задачами (Problem).
 *
 * Предоставляет CRUD-операции над сущностью [Problem].
 * Все методы выполняются в рамках транзакции благодаря аннотации [@Transactional].
 *
 * @property problemRepository репозиторий для работы с таблицей `problems`
 * @property meterRegistry реестр метрик для отслеживания событий
 */
@Service
@Transactional
class ProblemService(
    private val problemRepository: ProblemRepository,
    private val solutionRepository: SolutionRepository,
    private val meterRegistry: MeterRegistry
) {

    private val logger = KotlinLogging.logger {}

    /**
     * Создаёт новую задачу и сохраняет её в базе данных.
     *
     * @param name название задачи (обязательное поле)
     * @param description описание задачи (опциональное, может быть `null`)
     * @param condition условие задачи (опциональное, может быть `null`)
     * @return созданная задача с присвоенным идентификатором
     */
    fun create(name: String, description: String?, condition: String? = null): Problem {
        logger.info { "Создание задачи: $name" }
        logger.debug { "Параметры создания: name='$name', description='$description', condition='$condition'" }
        
        return try {
            val newProblem = Problem(name = name, description = description, condition = condition)
            logger.debug { "Объект Problem создан перед сохранением: $newProblem" }
            
            val saved = problemRepository.save(newProblem)
            logger.info { "Задача успешно создана: id=${saved.id}, name='${saved.name}'" }
            logger.debug { "Сохранённое значение: $saved" }
            
            meterRegistry.counter("problem.created.total").increment()
            saved
        } catch (e: Exception) {
            logger.error { "Ошибка при создании задачи: ${e.message}" }
            logger.debug { "Stack trace: ${e.stackTrace.joinToString("\n")}" }
            meterRegistry.counter("problem.created.failed.total").increment()
            throw e
        }
    }

    /**
     * Находит задачу по её идентификатору.
     *
     * @param id идентификатор задачи
     * @return задача если найдена, иначе `null`
     */
    fun findById(id: Long): Problem? {
        logger.debug { "Поиск задачи по id=$id" }
        val result = problemRepository.findById(id).orElse(null)
        if (result != null) {
            logger.debug { "Задача найдена: id=$id, name='${result.name}'" }
        } else {
            logger.debug { "Задача не найдена: id=$id" }
        }
        return result
    }

    /**
     * Возвращает список всех задач из базы данных.
     *
     * @return список всех задач, пустой список если задач нет
     */
    fun findAll(): List<Problem> {
        logger.debug { "Получение списка всех задач" }
        val result = problemRepository.findAll()
        logger.debug { "Получено задач: ${result.size}" }
        return result
    }

    /**
     * Обновляет поля существующей задачи.
     *
     * Обновляются только те поля, которые переданы не `null`.
     * Если задача с указанным [id] не найдена — возвращает `null`.
     *
     * @param id идентификатор задачи для обновления
     * @param name новое название задачи, если `null` — название не изменяется
     * @param description новое описание задачи, если `null` — описание не изменяется
     * @param condition новое условие задачи, если `null` — условие не изменяется
     * @return обновлённая задача, или `null` если задача не найдена
     */
    fun update(id: Long, name: String?, description: String?, condition: String? = null): Problem? {
        logger.info { "Обновление задачи id=$id: name=$name, description=$description, condition=$condition" }
        logger.debug { "Параметры обновления: id=$id, newName=$name, newDescription=$description, newCondition=$condition" }
        
        val problem = findById(id)
        if (problem == null) {
            logger.warn { "Задача не найдена для обновления: id=$id" }
            logger.debug { "Операция update отменена: задача с id=$id не существует" }
            meterRegistry.counter("problem.update.failed.not_found.total").increment()
            return null
        }
        
        val oldName = problem.name
        val oldDescription = problem.description
        val oldCondition = problem.condition

        name?.let {
            logger.debug { "Изменение названия: '$oldName' -> '$it'" }
            problem.name = it
        }
        
        description?.let {
            logger.debug { "Изменение описания: '$oldDescription' -> '$it'" }
            problem.description = it
        }

        condition?.let {
            logger.debug { "Изменение условия: '$oldCondition' -> '$it'" }
            problem.condition = it
        }
        
        val updated = problemRepository.save(problem)
        logger.info { "Задача успешно обновлена: id=${updated.id}, name='${updated.name}'" }
        logger.debug { "Обновлённое значение: $updated" }
        
        meterRegistry.counter("problem.updated.total").increment()
        return updated
    }

     /**
      * Удаляет задачу по её идентификатору.
      *
      * Если задача с указанным [id] не существует — выбрасывает [ResourceNotFoundException].
      *
      * @param id идентификатор задачи для удаления
      * @throws ResourceNotFoundException если задача не найдена
      */
     fun delete(id: Long) {
         logger.info { "Начало удаления задачи id=$id" }
         logger.debug { "Выполняется DELETE операция для задачи id=$id" }

         if (!problemRepository.existsById(id)) {
             logger.warn { "Попытка удаления несуществующей задачи: id=$id" }
             logger.debug { "Task с id=$id не найдена в базе данных" }
             meterRegistry.counter("problem.deleted.failed.not_found.total").increment()
             throw ResourceNotFoundException("Problem with id=$id not found")
         }

         return try {
             val problem = problemRepository.findById(id)
                 .orElseThrow { ResourceNotFoundException("Problem with id=$id not found") }
             solutionRepository.deleteAllByProblem(problem)
             problemRepository.deleteById(id)
             logger.info { "Задача успешно удалена: id=$id" }
             logger.debug { "Операция DELETE завершена успешно для id=$id" }
             meterRegistry.counter("problem.deleted.total").increment()
         } catch (e: Exception) {
             logger.error { "Ошибка при удалении задачи id=$id: ${e.message}" }
             logger.debug { "Stack trace: ${e.stackTrace.joinToString("\n")}" }
             meterRegistry.counter("problem.deleted.failed.total").increment()
             throw e
         }
     }
}
