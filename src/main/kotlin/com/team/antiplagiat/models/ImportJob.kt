package com.team.antiplagiat.models

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "import_jobs")
class ImportJob(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    var admin: User = User(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ImportJobStatus = ImportJobStatus.PENDING,

    @Column(name = "started_at")
    var startedAt: LocalDateTime? = null,

    @Column(name = "finished_at")
    var finishedAt: LocalDateTime? = null,

    @Column(name = "file_name", nullable = false)
    var fileName: String = "",

    @Column(name = "imported_solutions", nullable = false)
    var importedSolutions: Int = 0,

    @Column(name = "created_problems", nullable = false)
    var createdProblems: Int = 0,

    @Column(name = "skipped_files", nullable = false)
    var skippedFiles: Int = 0,

    @Column(name = "users_matched", nullable = false)
    var usersMatched: Int = 0,

    @Column(name = "users_not_found", nullable = false)
    var usersNotFound: Int = 0,

    @Column(columnDefinition = "TEXT")
    var errors: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

