package com.team.antiplagiat.models.user

data class User(
    override val id: Long,
    override var login: String,
    override var email: String
) : BasicUser