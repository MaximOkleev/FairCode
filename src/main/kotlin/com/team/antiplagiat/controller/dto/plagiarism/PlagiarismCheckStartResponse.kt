package com.team.antiplagiat.controller.dto.plagiarism

data class PlagiarismCheckStartResponse(
    val runId: Long,
    val status: String,
    val message: String
)
