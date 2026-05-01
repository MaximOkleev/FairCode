package com.team.antiplagiat.models

enum class SolutionStatus {
    WAITING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED;

    companion object {
        fun fromString(status: String): SolutionStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Неизвестный статус: $status")
            }
        }
    }
}
