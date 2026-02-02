package com.team.antiplagiat.service

import com.team.antiplagiat.models.Solution
import java.util.Properties

class SolutionService(
    private val properties: AntiPlagiatProperties
) {
    private val solutions = mutableMapOf<Long, Solution>()
    private val attemptsCounter = mutableMapOf<Pair<Long, Long>, Int>()

    fun create(solution: Solution): Boolean{
        println("Попытка создать посылку: $solution")

        val key = solution.userId to solution.taskId
        val attempts = attemptsCounter.getOrDefault(key, 0)

        if (attempts >= properties.maxAttempts) {
            println("Превышено кол-во попыток ${properties.maxAttempts} для userId ${solution.userId}, taskId ${solution.taskId} ")
            return false
        }

        solutions[solution.id] = solution
        attemptsCounter[key] = attempts + 1
        println("Посылка создана. id ${solution.id}, попытка ${attemptsCounter[key]}/${properties.maxAttempts}")
        return true
    }

    fun read(id: Long): Solution? {
        val solution = solutions[id]
        println("Чтение посылки id $id")
        return solution
    }

    fun update(id: Long, updated: Solution): Boolean {
        if (!solutions.containsKey(id)) {
            println("Обновление невозможно, т.к. посылка id $id не найдена")
            return false
        }

        solutions[id] = updated
        println("Посылка обновлена, id $id")
        return true
    }

    fun delete(id: Long): Boolean {
        val removes = solutions.remove(id)
        if (removes == null) {
            println("Посылка $id не найдена, невозможно удалить")
            return false
        }

        println("Посылка $id удалена")
        return true
    }

}