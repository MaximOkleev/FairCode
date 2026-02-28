package com.team.antiplagiat.models

import java.time.LocalDateTime

data class Contest(
    override val id: Long,
    var name : String,
    val adminId : Long,
    val startedAt : LocalDateTime,
    var duration : Long,
) : Entity