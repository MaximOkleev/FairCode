package com.team.antiplagiat.service

import com.team.antiplagiat.models.User
import com.team.antiplagiat.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class UserService(private val userRepository: UserRepository) {

    fun create(user: User): User = userRepository.save(user)

    fun findById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun findAll(): List<User> = userRepository.findAll()

    fun update(id: Long, login: String?, email: String?): User? {
        val user = findById(id) ?: return null
        login?.let { user.login = it }
        email?.let { user.email = it }
        return userRepository.save(user)
    }
    fun delete(id: Long) = userRepository.deleteById(id)
}