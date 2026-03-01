package com.team.antiplagiat.service

import com.team.antiplagiat.models.user.User
import org.springframework.stereotype.Service
import io.github.oshai.kotlinlogging.KotlinLogging

@Service
class UserCRUD : ServiceCRUD<User> {

    private val logger = KotlinLogging.logger {}

    override val entities: MutableMap<Long, User> = mutableMapOf()

    fun update(id: Long, login: String?, email: String?): Boolean {
        val user = entities[id]
        if (user == null) {
            logger.warn { "Пользователь с id = $id не найден" }
            return false
        }
        if (login != null) user.login = login
        if (email != null) user.email = email
        logger.info { "Пользователь с id = $id успешно обновлен" }
        return true
    }
}