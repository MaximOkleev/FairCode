package com.team.antiplagiat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

private val logger = KotlinLogging.logger {}

@SpringBootApplication
@EnableAsync

class AntiplagiatApplication

fun main(args: Array<String>) {
    logger.info { "Запуск Antiplagiat Application..." }
    val app = runApplication<AntiplagiatApplication>(*args)
    logger.info { "Antiplagiat Application успешно запущена!" }
}
