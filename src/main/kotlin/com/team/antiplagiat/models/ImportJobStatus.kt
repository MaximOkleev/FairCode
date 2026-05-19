package com.team.antiplagiat.models

enum class ImportJobStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED;

    companion object {
        fun fromString(status: String): ImportJobStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Неизвестный статус импорта: $status")
            }
        }
    }
}

