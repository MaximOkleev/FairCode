package com.team.antiplagiat.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Check
import java.time.LocalDateTime

@Entity
@Check(constraints = "first_solution_id < second_solution_id")
@Table(
    name = "plagiarism_matches",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_plagiarism_matches_run_solution_pair",
            columnNames = ["check_run_id", "first_solution_id", "second_solution_id"]
        )
    ],
    indexes = [
        Index(name = "idx_plagiarism_matches_check_run", columnList = "check_run_id"),
        Index(name = "idx_plagiarism_matches_first_solution", columnList = "first_solution_id"),
        Index(name = "idx_plagiarism_matches_second_solution", columnList = "second_solution_id"),
        Index(name = "idx_plagiarism_matches_detected_at", columnList = "detected_at")
    ]
)
class PlagiarismMatch(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_run_id", nullable = false)
    var checkRun: PlagiarismCheckRun = PlagiarismCheckRun(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_solution_id", nullable = false)
    var firstSolution: Solution = Solution(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "second_solution_id", nullable = false)
    var secondSolution: Solution = Solution(),

    @Column(nullable = false)
    var similarity: Double = 0.0,

    @Column(nullable = false)
    var threshold: Double = 0.0,

    @Column(name = "detected_at", nullable = false)
    var detectedAt: LocalDateTime = LocalDateTime.now()
)
