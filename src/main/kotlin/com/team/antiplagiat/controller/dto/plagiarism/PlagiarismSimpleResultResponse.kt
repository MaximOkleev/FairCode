package com.team.antiplagiat.controller.dto.plagiarism

data class PlagiarismSimpleResultResponse(
    val problem: String,
    val user1: String,
    val user2: String,
    val similarity: Double
)