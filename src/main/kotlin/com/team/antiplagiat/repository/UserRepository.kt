package com.team.antiplagiat.repository

import com.team.antiplagiat.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long> {
    fun findByLogin(login: String): User?
    fun findByEmail(email: String): User?

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    fun markEmailVerified(@Param("userId") userId: Long)
}