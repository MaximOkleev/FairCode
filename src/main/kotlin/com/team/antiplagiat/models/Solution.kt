package com.team.antiplagiat.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "solutions",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "contest_id", "problem_id", "file_path"])]
)
class Solution(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @ManyToOne
    @JoinColumn(name = "contest_id")
    var contest: Contest? = null,

    @Column(nullable = false)
    var language: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: SolutionStatus = SolutionStatus.WAITING,

    @Column(name = "submitted_at", nullable = false)
    var submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "file_path", nullable = false)
    var filePath: String = "",

    @Column(columnDefinition = "TEXT")
    var code: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
