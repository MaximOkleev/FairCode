package com.team.antiplagiat.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var login: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(
        nullable = false,
        columnDefinition = "VARCHAR(255) DEFAULT '$2a\$10\$fIZ0W0dNFtI5QZbJvY1I6OZzg4IJs6Uo2dLcXrOh/ZLmZrSv5fYO2'"
    )
    var passwordHash: String = "\$2a\$10\$fIZ0W0dNFtI5QZbJvY1I6OZzg4IJs6Uo2dLcXrOh/ZLmZrSv5fYO2",

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var role: Role = Role.BASIC,

    @Column
    var resetToken: String? = null,

    @Column
    var resetTokenExpiry: Instant? = null
) {
    enum class Role { ADMIN, BASIC }
}