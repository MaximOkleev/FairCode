package com.team.antiplagiat.runner

import com.team.antiplagiat.models.user.User
import com.team.antiplagiat.service.UserCRUD
import com.team.antiplagiat.service.ContestCRUD
import com.team.antiplagiat.service.ProblemCrudService
import org.springframework.boot.CommandLineRunner
import com.team.antiplagiat.models.Contest
import com.team.antiplagiat.models.Solution
import com.team.antiplagiat.service.SolutionCRUD
import java.time.LocalDateTime
import org.springframework.stereotype.Component
import io.github.oshai.kotlinlogging.KotlinLogging

@Component
class CommandLineRunner(
    private val problemService: ProblemCrudService,
    private val userService: UserCRUD,
    private val contestService: ContestCRUD,
    private val solutionCRUD: SolutionCRUD
) : CommandLineRunner {

    private val logger = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        logger.info { "=".repeat(70) }
        logger.info { "ДЕМОНСТРАЦИЯ РАБОТЫ CRUD СЕРВИСОВ" }
        logger.info { "=".repeat(70) }

        logger.info { "\nЧАСТЬ 1: ДЕМОНСТРАЦИЯ UserCRUD" }
        logger.info { "-".repeat(30) }

        logger.info { "\n1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЕЙ:" }
        val user1 = User(1, "user1", "user1@example.com")
        val user2 = User(2, "user2", "user2@example.com")

        userService.create(user1)
        userService.create(user2)

        logger.info { "\n2. ЧТЕНИЕ ПОЛЬЗОВАТЕЛЯ:" }
        val foundUser = userService.read(1)
        logger.info { "Найден пользователь: ${foundUser?.login} (email: ${foundUser?.email})" }

        logger.info { "\n3. ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ:" }
        userService.update(1, "updated_user", "new_email@example.com")

        val updatedUser = userService.read(1)
        logger.info { "Обновленный email: ${updatedUser?.email}" }

        logger.info { "\n4. УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ:" }
        userService.delete(2)

        logger.info { "\nЧАСТЬ 2: ДЕМОНСТРАЦИЯ ProblemCrudService" }
        logger.info { "-".repeat(30) }

        logger.info { "\n1. СОЗДАНИЕ ЗАДАЧ:" }
        problemService.createProblem("Сумма чисел", "Найти сумму двух чисел")
        problemService.createProblem("Факториал", "Вычислить факториал числа")
        problemService.createProblem("Палиндром", "Проверить строку на палиндром")

        logger.info { "\n2. ЧТЕНИЕ ВСЕХ ЗАДАЧ:" }
        val allProblems = problemService.getAllProblems()
        logger.info { "Всего задач: ${allProblems.size}" }
        allProblems.forEach { problem ->
            logger.info { "  ID: ${problem.id}, Название: '${problem.name}'" }
        }

        logger.info { "\n3. ЧТЕНИЕ ЗАДАЧИ ПО ID:" }
        val problem = problemService.read(1)
        logger.info { "Задача с ID=1: ${problem?.name} - ${problem?.description}" }

        logger.info { "\n4. ОБНОВЛЕНИЕ ЗАДАЧИ:" }
        problemService.updateProblem(1, "Сумма двух чисел", "Сложить 2 числа")

        logger.info { "\n5. УДАЛЕНИЕ ЗАДАЧИ:" }
        problemService.delete(2)

        logger.info { "\n6. ФИНАЛЬНОЕ СОСТОЯНИЕ:" }
        val finalProblems = problemService.getAllProblems()
        logger.info { "Осталось задач: ${finalProblems.size}" }

        logger.info { "\n" + "=".repeat(70) }
        logger.info { "ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА" }
        logger.info { "=".repeat(70) }

        logger.info { "\nЧАСТЬ 3: ДЕМОНСТРАЦИЯ ContestCRUD" }
        logger.info { "-".repeat(30) }

        logger.info { "\n1. СОЗДАНИЕ КОНТЕСТОВ:" }
        val contest1 = Contest(1, "A", 100, LocalDateTime.now().plusDays(7), 3 * 3600)
        val contest2 = Contest(2, "О", 200, LocalDateTime.now().plusDays(3), 5 * 3600)
        val contest3 = Contest(3, "ПЕ", 100, LocalDateTime.now().plusDays(1), 8 * 3600)
        contestService.create(contest1)
        contestService.create(contest2)
        contestService.create(contest3)

        logger.info { "\n2. СОЗДАНИЕ КОНТЕСТОВ ДЛЯ ПОИСКА ПО АДМИНУ:" }
        val contest4 = Contest(4, "Контест админа 100", 100, LocalDateTime.now().plusDays(2), 4 * 3600)
        val contest5 = Contest(5, "Контест админа 300", 300, LocalDateTime.now().plusDays(1), 2 * 3600)
        contestService.create(contest4)
        contestService.create(contest5)

        logger.info { "\n3. ЧТЕНИЕ КОНТЕСТА ПО ID:" }
        val foundContest = contestService.read(1)
        logger.info { "Найден контест: ${foundContest?.name} (длительность: ${foundContest?.duration?.div(3600)} часов)" }

        logger.info { "\n4. ОБНОВЛЕНИЕ КОНТЕСТА:" }
        contestService.update(1, "ПЕ", 4 * 3600)
        val updatedContest = contestService.read(1)
        logger.info { "Обновленный контест: '${updatedContest?.name}', ${updatedContest?.duration?.div(3600)} часов" }

        logger.info { "\n5. ПОИСК КОНТЕСТОВ ПО АДМИНУ:" }
        val admin100Contests = contestService.getByAdmin(100)
        if (admin100Contests != null) {
            logger.info { "Контесты админа 100:" }
            admin100Contests.forEach { contest ->
                logger.info { "  • '${contest.name}' (${contest.duration / 3600} часов)" }
            }
        }
        val admin300Contests = contestService.getByAdmin(300)
        if (admin300Contests != null) {
            logger.info { "Контесты админа 300:" }
            admin300Contests.forEach { contest ->
                logger.info { "  • '${contest.name}'" }
            }
        }

        logger.info { "\n6. УДАЛЕНИЕ КОНТЕСТА:" }
        contestService.delete(4)

        logger.info { "\n" + "=".repeat(70) }
        logger.info { "ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА" }
        logger.info { "=".repeat(70) }

        logger.info { "\nЧАСТЬ 4: ДЕМОНСТРАЦИЯ SolutionCRUD" }
        logger.info { "-".repeat(30) }
        val solution = Solution(10, 2, 1, "C++", "waiting", LocalDateTime.now())

        logger.info { "\n1. СОЗДАНИЕ ПОСЫЛОКИ:" }
        solutionCRUD.create(solution)

        logger.info { "\n2. ЛОВИМ ОГРАНИЧЕНИЕ:" }
        for (i in 1..51) {
            val solution = Solution(i.toLong(), 2, 1, "C++", "waiting", LocalDateTime.now())
            val res = solutionCRUD.create(solution)
            logger.info { "Попытка $i: ${if (res) "УСПЕШНО" else "ОТКЛОНЕНО (превышен лимит)"}" }
        }

        logger.info { "\n3. ЧТЕНИЕ ПОСЫЛОК:" }
        val res = solutionCRUD.read(1)
        logger.info { "  ID: ${res?.id}, Пользователь: ${res?.userId}, Задача: ${res?.taskId}" }

        logger.info { "\n4. ОБНОВЛЕНИЕ ПОСЫЛКИ:" }
        val updatedSolution = Solution(
            1,
            1,
            1,
            "Python",
            "waitng",
            LocalDateTime.now(),
        )
        solutionCRUD.update(1, updatedSolution)

        logger.info { "\n5. УДАЛЕНИЕ ПОСЫЛОК:" }
        solutionCRUD.delete(1)
        solutionCRUD.delete(2)

        logger.info { "\n" + "=".repeat(70) }
        logger.info { "ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА" }
        logger.info { "=".repeat(70) }
    }
}