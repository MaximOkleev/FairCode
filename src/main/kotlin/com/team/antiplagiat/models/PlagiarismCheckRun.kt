package com.team.antiplagiat.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "plagiarism_check_runs")
class PlagiarismCheckRun(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var threshold: Double = 0.0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PlagiarismCheckRunStatus = PlagiarismCheckRunStatus.PENDING,

    @Column(name = "checked_solutions", nullable = false)
    var checkedSolutions: Int = 0,

    @Column(name = "compared_pairs", nullable = false)
    var comparedPairs: Int = 0,

    @Column(nullable = false)
    var matches: Int = 0,

    @Column(nullable = false)
    var groups: Int = 0,

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "finished_at")
    var finishedAt: LocalDateTime? = null
)
