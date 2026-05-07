package com.team.antiplagiat.service

import com.team.antiplagiat.repository.UserRepository
import com.team.antiplagiat.config.TokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenService: TokenService
) {
    fun authenticate(login: String, password: String): String {
        val user = userRepository.findByLogin(login)
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        return tokenService.generateToken(user)
    }
}

