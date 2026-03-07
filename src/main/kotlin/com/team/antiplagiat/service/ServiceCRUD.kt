package com.team.antiplagiat.service

import com.team.antiplagiat.models.Entity

interface ServiceCRUD<T : Entity> {
    val entities: MutableMap<Long, T>
    fun create(entity: T): Boolean
    fun read(id: Long): T?
    fun delete(id: Long): Boolean
}