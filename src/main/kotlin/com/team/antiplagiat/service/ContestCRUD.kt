package com.team.antiplagiat.service
import com.team.antiplagiat.models.Contest
import org.springframework.stereotype.Service
import com.team.antiplagiat.config.ContestConfig

@Service
class ContestCRUD(private val config: ContestConfig): ServiceCRUD<Contest> {
    override val entities: MutableMap<Long, Contest> = mutableMapOf()

    override fun create(entity: Contest): Boolean {
        println("[INFO] Попытка создать контест: id=${entity.id}, name='${entity.name}'")
        val maxDurationSeconds = config.maxDurationHours * 3600
        if (entity.duration > maxDurationSeconds) {
            println("[ERROR] Длительность контеста слишком большая!")
            println("[ERROR] Текущая: ${entity.duration} сек. (${entity.duration / 3600} часов)")
            println("[ERROR] Максимально разрешенная: $maxDurationSeconds сек. (${config.maxDurationHours} часов)")
            return false
        }
        if (entities.containsKey(entity.id)) {
            println("[WARN] Конкурс с id=${entity.id} уже существует")
            return false
        }

        entities[entity.id] = entity
        println("[INFO] Контест создан: id=${entity.id}")
        return true
    }

    fun update(id: Long, name: String?, duration: Long?): Boolean {
        println("[INFO] Обновление контест id=$id, name='$name', duration=$duration")
        val contest = entities[id] ?: run {
            println("[WARN] Конкурс id=$id не найден")
            return false
        }
        println("[INFO] До: name='${contest.name}', duration=${contest.duration}")
        if (duration != null) {
            val maxDurationSeconds = config.maxDurationHours * 3600
            if (duration > maxDurationSeconds) {
                println("[ERROR] Новая длительность слишком большая!")
                println("[ERROR] Запрошено: $duration сек. (${duration / 3600} часов)")
                println("[ERROR] Максимально разрешенная: $maxDurationSeconds сек. (${config.maxDurationHours} часов)")
                return false
            }
        }
        if (name != null) contest.name = name
        if (duration != null) contest.duration = duration
        println("[INFO] После: name='${contest.name}', duration=${contest.duration}")
        return true
    }

    override fun read(id: Long): Contest? {
        println("[INFO] Поиск контеста с id=$id")
        val contest = entities[id]
        if (contest == null) {
            println("[WARN] Контест с id=$id не найден")
            println("[DEBUG] Доступные ID контестов: ${entities.keys.sorted().joinToString()}")
            return null
        } else {
            println("[INFO] Контест найден: id=${contest.id}, name='${contest.name}'")
            println("Подробности контеста: $contest")
            return contest
        }
    }

    fun getByAdmin(adminId: Long): List<Contest>? {
        println("[INFO] Поиск контестов администратора adminId=$adminId")
        val adminContests = entities.values.filter { it.adminId == adminId }.sortedByDescending { it.startedAt }.toList()
        if (adminContests.isEmpty()) {
            println("[WARN] У администратора adminId=$adminId нет контестов")
            println("[DEBUG] Всего контестов в системе: ${entities.size}")
            return null
        } else {
            println("[INFO] Найдено ${adminContests.size} контестов для adminId=$adminId")
            adminContests.forEachIndexed { index, contest ->
                println(
                    "Контест ${index + 1}: id=${contest.id}, name='${contest.name}', " + "started=${contest.startedAt}, duration=${contest.duration}"
                )
            }
            return adminContests
        }
    }

    override fun delete(id: Long): Boolean {
        println("[INFO] Удаление контеста id=$id")
        if (entities.remove(id) == null) {
            println("[WARN] Контест id=$id не найден для удаления")
            return false
        }
        println("[INFO] Контест id=$id удален")
        return true
    }
}