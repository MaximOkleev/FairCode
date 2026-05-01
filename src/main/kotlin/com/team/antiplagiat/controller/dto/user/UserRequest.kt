package com.team.antiplagiat.controller.dto.user

import com.team.antiplagiat.models.User

data class UserRequest(
    val login: String,
    val email: String,
    val role: User.Role
)

fun UserRequest.toEntity(): User = User(
    login = this.login,
    email = this.email,
    role = this.role
)

