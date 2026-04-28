package com.team.antiplagiat.config

import com.team.antiplagiat.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

/**
 * Загружает пользователей из базы данных для Spring Security.
 *
 * Поддерживает загрузку пользователей и их ролей из таблицы users.
 * Пароли хранятся в виде bcrypt-hash в поле password_hash.
 */
@Service
class DatabaseUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByLogin(username)
            ?: run {
                logger.warn { "User not found with login: $username" }
                throw UsernameNotFoundException("User not found: $username")
            }

        val authority = SimpleGrantedAuthority("ROLE_${user.role.name}")

        logger.debug { "Loaded user: $username with role: ${user.role}" }

        return User.builder()
            .username(user.login)
            .password(user.passwordHash)
            .authorities(authority)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build()
    }
}

