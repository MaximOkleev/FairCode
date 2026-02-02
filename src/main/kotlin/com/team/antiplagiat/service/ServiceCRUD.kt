package com.team.antiplagiat.service

import com.team.antiplagiat.models.Entity

interface ServiceCRUD<T : Entity> {
    val entities: MutableMap<Long, T>

    fun create(entity: T) : Boolean {
        if (entities.containsKey(entity.id)) return false
        entities[entity.id] = entity
        return true
    }

    fun read(id : Long) = entities[id]

    fun update() : Boolean

    fun delete(id: Long) = entities.remove(id) != null
}