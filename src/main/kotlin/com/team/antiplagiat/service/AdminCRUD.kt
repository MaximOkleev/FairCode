package com.team.antiplagiat.service

import com.team.antiplagiat.models.user.Admin
import org.springframework.stereotype.Service

@Service
class AdminCRUD : BaseServiceCRUD<Admin>() {

    fun update(id: Long, login: String?, email: String?): Boolean {
        logger.info { "Admin с id = $id " }
        if (entities[id] == null) {
            logger.warn { "не найден" }
            return false
        }
        val user = entities[id] ?: return false
        if (login != null) user.login = login
        if (email != null) user.email = email
        logger.info { "успешно обновлен" }
        return true
    }
}