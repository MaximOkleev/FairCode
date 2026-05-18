package com.team.antiplagiat.service

import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val meterRegistry: MeterRegistry
) {

    fun create(user: User): User {
        logger.info { "Создание пользователя: login=${user.login}, email=${user.email}" }
        val saved = userRepository.save(user)
        meterRegistry.counter("user.created.total").increment()
        logger.debug { "Пользователь сохранен: id=${saved.id}" }
        return saved
    }

    fun findById(id: Long): User? {
        logger.debug { "Поиск пользователя по id=$id" }
        val result = userRepository.findById(id).orElse(null)
        if (result == null) {
            meterRegistry.counter("user.read.not_found.total").increment()
            logger.debug { "Пользователь id=$id не найден" }
        } else {
            meterRegistry.counter("user.read.total").increment()
        }
        return result
    }

    fun findAll(): List<User> {
        logger.debug { "Получение списка пользователей" }
        val result = userRepository.findAll()
        meterRegistry.counter("user.read.all.total").increment()
        logger.debug { "Пользователей найдено: ${result.size}" }
        return result
    }

    fun update(id: Long, login: String?, email: String?): User? {
        logger.info { "Обновление пользователя id=$id" }
        val user = findById(id) ?: run {
            meterRegistry.counter("user.update.not_found.total").increment()
            logger.warn { "Пользователь id=$id не найден для обновления" }
            return null
        }
        login?.let { user.login = it }
        email?.let { user.email = it }
        val saved = userRepository.save(user)
        meterRegistry.counter("user.updated.total").increment()
        logger.debug { "Пользователь обновлен: id=${saved.id}" }
        return saved
    }

    fun delete(id: Long) {
        logger.info { "Удаление пользователя id=$id" }
        userRepository.deleteById(id)
        meterRegistry.counter("user.deleted.total").increment()
        logger.debug { "Пользователь id=$id удален" }
    }
}