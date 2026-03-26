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
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            taskId = rs.getLong("task_id"),
            language = rs.getString("language"),
            filePath = rs.getString("file_path"),
            date = rs.getTimestamp("created_at")?.toLocalDateTime() ?: LocalDateTime.now()
        )
    }

    fun add(solution: Solution): Long {
        val solutionId = jdbc.queryForObject(
            """
        INSERT INTO solutions (user_id, task_id, language, file_path, created_at)
        VALUES (?, ?, ?, ?, ?)
        RETURNING id
        """.trimIndent(),
            Long::class.java,
            solution.userId,
            solution.taskId,
            solution.language,
            solution.filePath,
            solution.date
        )
        return solutionId ?: 0
    }

    fun count(): Int =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM solutions",
            Int::class.java
        ) ?: 0

    fun findById(id: Long): Solution? =
        jdbc.query(
            "SELECT * FROM solutions WHERE id = ?",
            rowMapper,
            id
        ).firstOrNull()

    fun findAll(): List<Solution> =
        jdbc.query(
            "SELECT * FROM solutions ORDER BY created_at DESC",
            rowMapper
        )

}
