package com.team.antiplagiat.models.user.service

import com.team.antiplagiat.models.user.User
import com.team.antiplagiat.service.ServiceCRUD
import org.springframework.stereotype.Component

@Component
class UserCRUD : ServiceCRUD<User> {

    override val entities: MutableMap<Long, User> = mutableMapOf()

    fun update(id: Long, login: String?, email: String?): Boolean {
        val user = entities[id] ?: return false
        if (login != null) user.login = login
        if (email != null) user.email = email
        return true
    }
}