package com.team.antiplagiat.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "ai_generated_solutions")
class AiGeneratedSolution(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "problem_id", nullable = false)
    var problem: Problem = Problem(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var provider: AiProvider = AiProvider.CHATGPT,

    @Column(nullable = false)
    var language: String = "",

    @Column(name = "model_name", nullable = false)
    var modelName: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AiGeneratedSolutionStatus = AiGeneratedSolutionStatus.SUCCESS,

    @Column(columnDefinition = "TEXT", nullable = false)
    var prompt: String = "",

    @Column(columnDefinition = "TEXT")
    var code: String? = null,

    @Column(name = "error_message", columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @Column(name = "generated_at", nullable = false)
    var generatedAt: LocalDateTime = LocalDateTime.now()
)
