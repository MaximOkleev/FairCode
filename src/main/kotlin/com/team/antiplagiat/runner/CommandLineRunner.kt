package com.team.antiplagiat.runner

import com.team.antiplagiat.models.user.User
import com.team.antiplagiat.models.user.service.UserCRUD
import com.team.antiplagiat.service.ProblemCrudService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CommandLineRunnner(
    private val problemService: ProblemCrudService,
    private val userService: UserCRUD
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(CommandLineRunnner::class.java)

    override fun run(vararg args: String?) {
        logger.info("\n" + "=".repeat(70))
        logger.info("ДЕМОНСТРАЦИЯ РАБОТЫ CRUD СЕРВИСОВ")
        logger.info("=".repeat(70))

        // Часть 1: Демонстрация работы с UserCRUD
        logger.info("\n" + "-".repeat(30))
        logger.info("ЧАСТЬ 1: РАБОТА С UserCRUD")
        logger.info("-".repeat(30))

        // Создаем пользователей
        logger.info("\n1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЕЙ:")
        val user1 = User(1, "user1", "user1@example.com")
        val user2 = User(2, "user2", "user2@example.com")

        val user1Created = userService.create(user1)
        val user2Created = userService.create(user2)

        logger.info("Пользователь 1 создан: $user1Created")
        logger.info("Пользователь 2 создан: $user2Created")

        // Часть 2: Демонстрация работы с ProblemCrudService
        logger.info("\n\n" + "-".repeat(30))
        logger.info("ЧАСТЬ 2: РАБОТА С ProblemCrudService")
        logger.info("-".repeat(30))

        // Создание задач
        logger.info("\n1. СОЗДАНИЕ ЗАДАЧ:")
        val task1Created = problemService.createProblem("Сумма чисел", "Найти сумму двух чисел")
        logger.info("Задача 'Сумма чисел' создана: $task1Created")

        val task2Created = problemService.createProblem("Факториал", "Вычислить факториал числа")
        logger.info("Задача 'Факториал' создана: $task2Created")

        logger.info("\n" + "=".repeat(70))
        logger.info("ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА")
        logger.info("=".repeat(70))
    }
}