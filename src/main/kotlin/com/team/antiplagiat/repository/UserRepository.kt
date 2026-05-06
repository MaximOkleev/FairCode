package com.team.antiplagiat.repository


import com.team.antiplagiat.models.User
import org.springframework.data.jpa.repository.JpaRepository



interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): User?
    fun findByEmail(email: String): User?
}