package com.team.antiplagiat.controller.dto.zipimport

data class ZipImportResponse(
    val problemsCreated: Int,
    val solutionsCreated: Int,
    val skippedFiles: Int,
    val usersMatched: Int,
    val usersNotFound: Int,
    val errors: List<String>
)