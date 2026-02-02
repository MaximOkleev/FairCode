package com.team.antiplagiat.runner

import com.team.antiplagiat.models.user.User
import com.team.antiplagiat.models.user.service.UserCRUD
import com.team.antiplagiat.service.ProblemCrudService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class CommandLineRunner(
    private val problemService: ProblemCrudService,
    private val userService: UserCRUD
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        println("=".repeat(70))
        println("ДЕМОНСТРАЦИЯ РАБОТЫ CRUD СЕРВИСОВ")
        println("=".repeat(70))

        println("\nЧАСТЬ 1: ДЕМОНСТРАЦИЯ UserCRUD")
        println("-".repeat(30))

        // CREATE
        println("\n1. СОЗДАНИЕ ПОЛЬЗОВАТЕЛЕЙ:")
        val user1 = User(1, "user1", "user1@example.com")
        val user2 = User(2, "user2", "user2@example.com")

        userService.create(user1)
        userService.create(user2)

        // READ
        println("\n2. ЧТЕНИЕ ПОЛЬЗОВАТЕЛЯ:")
        val foundUser = userService.read(1)
        println("Найден пользователь: ${foundUser?.login} (email: ${foundUser?.email})")

        // UPDATE
        println("\n3. ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ:")
        userService.update(1, "updated_user", "new_email@example.com")

        // Проверка обновления
        val updatedUser = userService.read(1)
        println("Обновленный email: ${updatedUser?.email}")

        // DELETE
        println("\n4. УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ:")
        userService.delete(2)

        println("\nЧАСТЬ 2: ДЕМОНСТРАЦИЯ ProblemCrudService")
        println("-".repeat(30))

        // CREATE
        println("\n1. СОЗДАНИЕ ЗАДАЧ:")
        problemService.createProblem("Сумма чисел", "Найти сумму двух чисел")
        problemService.createProblem("Факториал", "Вычислить факториал числа")
        problemService.createProblem("Палиндром", "Проверить строку на палиндром")

        // READ
        println("\n2. ЧТЕНИЕ ВСЕХ ЗАДАЧ:")
        val allProblems = problemService.getAllProblems()
        println("Всего задач: ${allProblems.size}")
        allProblems.forEach { problem ->
            println("  ID: ${problem.id}, Название: '${problem.name}'")
        }

        // READ BY ID
        println("\n3. ЧТЕНИЕ ЗАДАЧИ ПО ID:")
        val problem = problemService.read(1)
        println("Задача с ID=1: ${problem?.name} - ${problem?.description}")

        // UPDATE
        println("\n4. ОБНОВЛЕНИЕ ЗАДАЧИ:")
        problemService.updateProblem(1, "Сумма двух чисел", "Сложить 2 числа")

        // DELETE
        println("\n5. УДАЛЕНИЕ ЗАДАЧИ:")
        problemService.delete(2)

        println("\n6. ФИНАЛЬНОЕ СОСТОЯНИЕ:")
        val finalProblems = problemService.getAllProblems()
        println("Осталось задач: ${finalProblems.size}")

        println("\n" + "=".repeat(70))
        println("ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА")
        println("=".repeat(70))
    }
}