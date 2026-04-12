package com.team.antiplagiat.controller.dto

import com.team.antiplagiat.models.User


data class UserRequest(
    val login: String,
    val email: String,
    val role: User.Role
)


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

fun UserRequest.toEntity(): User = User(
    login = this.login,
    email = this.email,
    role = this.role
)