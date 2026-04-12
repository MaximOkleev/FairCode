package com.team.antiplagiat.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "contests")
class Contest(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    var admin: User,

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var duration: Int = 0,

    @ManyToMany
    @JoinTable(
        name = "contest_problems",
        joinColumns = [JoinColumn(name = "contest_id")],
        inverseJoinColumns = [JoinColumn(name = "problem_id")]
    )
    var problems: MutableSet<Problem> = mutableSetOf()
)