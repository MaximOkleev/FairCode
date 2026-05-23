package com.team.antiplagiat.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "ai_plagiarism_matches")
class AiPlagiarismMatch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_run_id", nullable = false)
    var checkRun: AiPlagiarismCheckRun = AiPlagiarismCheckRun(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solution_id", nullable = false)
    var solution: Solution = Solution(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_solution_id", nullable = false)
    var aiSolution: AiGeneratedSolution = AiGeneratedSolution(),

    @Column(nullable = false)
    var similarity: Double = 0.0,

    @Column(nullable = false)
    var threshold: Double = 0.0,

    @Column(name = "detected_at", nullable = false)
    var detectedAt: LocalDateTime = LocalDateTime.now()
)
