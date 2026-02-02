package com.team.antiplagiat.service

import com.team.antiplagiat.models.Entity

interface ServiceCRUD<T : Entity> {
    val entities: MutableMap<Long, T>

    fun create(entity: T) : Boolean {
        print("${entities::class.simpleName} с id = ${entity.id} ")
        if (entities.containsKey(entity.id)) {
            print("уже был создан\n")
            return false
        }
        entities[entity.id] = entity
        print("успешно создан")
        return true
    }

    fun read(id : Long): T? {
        val entity = entities[id]
        print("${entities::class.simpleName} c id = $id ")
        if (entity == null) {
            print("не существует\n")
            return null
        }
        print("успешно найден\n")
        return entity
    }

    fun delete(id: Long): Boolean {
        print("${entities::class.simpleName} с id = $id ")
        val res = entities.remove(id) != null
        if (res) print("успешно удален")
        else print("не найден")
        return res
    }
}