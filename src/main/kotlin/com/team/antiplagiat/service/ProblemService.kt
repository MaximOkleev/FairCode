package com.team.antiplagiat.service

import com.team.antiplagiat.models.Problem
import com.team.antiplagiat.repository.ProblemRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProblemService(private val problemRepository: ProblemRepository) {

    private val logger = KotlinLogging.logger {}

    fun create(name: String, description: String?): Problem {
        logger.info { "Создание задачи: $name" }
        return problemRepository.save(Problem(name = name, description = description))
    }

    fun findById(id: Long): Problem? = problemRepository.findById(id).orElse(null)

    fun findAll(): List<Problem> = problemRepository.findAll()

    fun update(id: Long, name: String?, description: String?): Problem? {
        val problem = findById(id) ?: return null
        name?.let { problem.name = it }
        description?.let { problem.description = it }
        return problemRepository.save(problem)
    }

    fun delete(id: Long) {
        logger.info { "Удаление задачи id=$id" }
        problemRepository.deleteById(id)
    }
}
