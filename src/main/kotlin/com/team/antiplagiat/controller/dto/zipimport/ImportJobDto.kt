package com.team.antiplagiat.controller.dto.zipimport

import java.time.LocalDateTime

data class ImportJobDto(
    val id: Long,
    val status: String,
    val fileName: String,
    val startedAt: LocalDateTime,
    val finishedAt: LocalDateTime?,
    val importedSolutions: Int,
    val createdProblems: Int,
    val skippedFiles: Int,
    val errors: List<String>
)

