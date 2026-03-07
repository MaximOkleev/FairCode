package com.team.antiplagiat.service

import com.team.antiplagiat.models.user.User
import org.springframework.stereotype.Service

@Service
class UserCRUD : BaseServiceCRUD<User>() {

    fun update(id: Long, login: String?, email: String?): Boolean {
        logger.info { "Обновление пользователя id=$id" }
        val user = entities[id] ?: return false.also { logger.warn { "Пользователь id=$id не найден" } }
        if (login != null) user.login = login
        if (email != null) user.email = email
        logger.info { "Пользователь id=$id успешно обновлён" }
        return true
    }
}