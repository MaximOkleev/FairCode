package com.team.antiplagiat.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "solutions")
class Solution(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @Column(nullable = false)
    var language: String = "",

    @Column(nullable = false)
    var status: String = "waiting",

    @Column(name = "submitted_at", nullable = false)
    var submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "file_path", nullable = false)
    var filePath: String = "",

    var code: String? = null
)