package com.team.antiplagiat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

private val logger = KotlinLogging.logger {}

@SpringBootApplication

class AntiplagiatApplication

fun main(args: Array<String>) {
    logger.info { "Запуск Antiplagiat Application..." }
    val app = runApplication<AntiplagiatApplication>(*args)
    logger.info { "Antiplagiat Application успешно запущена!" }
}
