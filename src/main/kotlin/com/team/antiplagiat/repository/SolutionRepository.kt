package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Solution
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class SolutionRepository(private val jdbc: JdbcTemplate) {

    private val rowMapper = RowMapper<Solution> { rs, _ ->
        Solution(
            id       = rs.getLong("id"),
            userId   = rs.getLong("admin_id"),
            taskId   = rs.getLong("id"),
            language = "C++",
            filePath = rs.getString("code"),
            date     = LocalDateTime.now()
        )
    }

    // Добавить решение (id = текущее количество решений)
    fun add(author: String, code: String): Boolean {
        val newId = count()
        return jdbc.update(
            "INSERT INTO solutions (id, author, code) VALUES (?, ?, ?)",
            newId, author, code
        ) > 0
    }

    // Получить общее количество решений
    fun count(): Int =
        jdbc.queryForObject("SELECT COUNT(*) FROM solutions", Int::class.java) ?: 0

    // Получить решение по ID
    fun findById(id: Long): Solution? =
        jdbc.query("SELECT * FROM solutions WHERE id = ?", rowMapper, id).firstOrNull()
}