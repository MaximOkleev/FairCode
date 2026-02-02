package com.team.antiplagiat.service

import com.team.antiplagiat.models.user.Admin
import org.springframework.stereotype.Service

@Service
class AdminCRUD : ServiceCRUD<Admin> {

    override val entities: MutableMap<Long, Admin> = mutableMapOf()

    fun update(id: Long, login: String?, email: String?): Boolean {
        print("Admin с id = $id ")
        if (entities[id] == null) {
            print("не найден\n")
            return false
        }
        val user = entities[id] ?: return false
        if (login != null) user.login = login
        if (email != null) user.email = email
        println("успешно обновлен\n")
        return true
    }
}