package com.team.antiplagiat.models

import java.time.LocalDateTime

data class Solution(
    override val id: Long,
    val userId: Long,
    val taskId: Long,
    val language: String,
    val filePath: String,
    val date: LocalDateTime
) : Entity