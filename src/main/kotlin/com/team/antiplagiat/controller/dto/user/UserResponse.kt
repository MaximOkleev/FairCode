package com.team.antiplagiat.controller.dto.user

import com.team.antiplagiat.models.User

data class UserResponse(
    val id: Long,
    val login: String,
    val email: String,
    val role: User.Role
) {
    companion object {
        fun fromEntity(user: User): UserResponse = UserResponse(
            id = user.id,
            login = user.login,
            email = user.email,
            role = user.role
        )
    }
}

