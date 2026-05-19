package com.team.antiplagiat.models

import jakarta.persistence.*

@Entity
@Table(name = "problems")
class Problem(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String = "",

    var description: String? = null
)
