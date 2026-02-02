package com.team.antiplagiat.runner

import com.team.antiplagiat.models.user.User
import com.team.antiplagiat.models.user.service.UserCRUD
import com.team.antiplagiat.service.ContestCRUD
import com.team.antiplagiat.service.ProblemCrudService
import org.springframework.boot.CommandLineRunner
import com.team.antiplagiat.models.Contest
import java.time.LocalDateTime
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
class CommandLineRunner(
    private val problemService: ProblemCrudService,
    private val userService: UserCRUD,
    private val contestService: ContestCRUD
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("=".repeat(70))
        println("ДЕМОНСТРАЦИЯ РАБОТЫ CRUD СЕРВИСОВ")
        println("=".repeat(70))

        println("\nЧАСТЬ 1: ДЕМОНСТРАЦИЯ UserCRUD")
        println("-".repeat(30))


        println("\n1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЕЙ:")
        val user1 = User(1, "user1", "user1@example.com")
        val user2 = User(2, "user2", "user2@example.com")

        userService.create(user1)
        userService.create(user2)


        println("\n2. ЧТЕНИЕ ПОЛЬЗОВАТЕЛЯ:")
        val foundUser = userService.read(1)
        println("Найден пользователь: ${foundUser?.login} (email: ${foundUser?.email})")

        println("\n3. ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ:")
        userService.update(1, "updated_user", "new_email@example.com")


        val updatedUser = userService.read(1)
        println("Обновленный email: ${updatedUser?.email}")

        println("\n4. УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ:")
        userService.delete(2)

        println("\nЧАСТЬ 2: ДЕМОНСТРАЦИЯ ProblemCrudService")
        println("-".repeat(30))

        println("\n1. СОЗДАНИЕ ЗАДАЧ:")
        problemService.createProblem("Сумма чисел", "Найти сумму двух чисел")
        problemService.createProblem("Факториал", "Вычислить факториал числа")
        problemService.createProblem("Палиндром", "Проверить строку на палиндром")

        println("\n2. ЧТЕНИЕ ВСЕХ ЗАДАЧ:")
        val allProblems = problemService.getAllProblems()
        println("Всего задач: ${allProblems.size}")
        allProblems.forEach { problem ->
            println("  ID: ${problem.id}, Название: '${problem.name}'")
        }

        println("\n3. ЧТЕНИЕ ЗАДАЧИ ПО ID:")
        val problem = problemService.read(1)
        println("Задача с ID=1: ${problem?.name} - ${problem?.description}")

        println("\n4. ОБНОВЛЕНИЕ ЗАДАЧИ:")
        problemService.updateProblem(1, "Сумма двух чисел", "Сложить 2 числа")


        println("\n5. УДАЛЕНИЕ ЗАДАЧИ:")
        problemService.delete(2)

        println("\n6. ФИНАЛЬНОЕ СОСТОЯНИЕ:")
        val finalProblems = problemService.getAllProblems()
        println("Осталось задач: ${finalProblems.size}")

        println("\n" + "=".repeat(70))
        println("ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА")
        println("=".repeat(70))



        println("\nЧАСТЬ 3: ДЕМОНСТРАЦИЯ ContestCRUD")
        println("-".repeat(30))

        println("\n1. СОЗДАНИЕ КОНТЕСТОВ:")
        val contest1 = Contest(1, "A", 100, LocalDateTime.now().plusDays(7), 3 * 3600)
        val contest2 = Contest(2, "О", 200, LocalDateTime.now().plusDays(3), 5 * 3600)
        val contest3 = Contest(3, "ПЕ", 100, LocalDateTime.now().plusDays(1), 8 * 3600)
        contestService.create(contest1)
        contestService.create(contest2)
        contestService.create(contest3)
        println("\n2. СОЗДАНИЕ ЕЩЕ КОНТЕСТОВ ДЛЯ ПОИСКА ПО АДМИНИСТРАТОРУ:")
        val contest4 = Contest(4, "Контест админа 100", 100, LocalDateTime.now().plusDays(2), 4 * 3600)
        val contest5 = Contest(5, "Контест админа 300", 300, LocalDateTime.now().plusDays(1), 2 * 3600)
        contestService.create(contest4)
        contestService.create(contest5)
        println("\n3. ЧТЕНИЕ КОНТЕСТА ПО ID:")
        val foundContest = contestService.read(1)
        println("Найден контест: ${foundContest?.name} (длительность: ${foundContest?.duration?.div(3600)} часов)")
        println("\n4. ВСЕ КОНТЕСТЫ:")
        val allContests = contestService.entities.values
        println("Всего контестов: ${allContests.size}")
        allContests.forEach { contest ->
            println("  ID: ${contest.id}, Название: '${contest.name}', Админ: ${contest.adminId}")
        }
        println("\n5. ОБНОВЛЕНИЕ КОНТЕСТА:")
        contestService.update(1, "ПЕ", 4 * 3600)
        val updatedContest = contestService.read(1)
        println("Обновленный контест: '${updatedContest?.name}', ${updatedContest?.duration?.div(3600)} часов")
        println("\n6. ПОИСК КОНТЕСТОВ ПО АДМИНИСТРАТОРУ:")
        val admin100Contests = contestService.getByAdmin(100)
        if (admin100Contests != null) {
            println("Контесты администратора 100:")
            admin100Contests.forEach { contest ->
                println("  • '${contest.name}' (${contest.duration / 3600} часов)")
            }
        }
        val admin300Contests = contestService.getByAdmin(300)
        if (admin300Contests != null) {
            println("Контесты администратора 300:")
            admin300Contests.forEach { contest ->
                println("  • '${contest.name}'")
            }
        }
        println("\n7. УДАЛЕНИЕ КОНТЕСТА:")
        contestService.delete(4)
        
        
    }
}