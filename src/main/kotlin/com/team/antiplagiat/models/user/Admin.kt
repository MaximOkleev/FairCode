package com.team.antiplagiat.models.user

data class Admin(
    override val id: Long,
    override var login: String,
    override var email: String
) : BasicUser