package com.team.antiplagiat.repository

import com.team.antiplagiat.models.Problem
import org.springframework.data.jpa.repository.JpaRepository


interface ProblemRepository : JpaRepository<Problem, Long> {
	fun findFirstByName(name: String): Problem?
}
