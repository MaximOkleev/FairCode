package com.team.antiplagiat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication

private val logger = KotlinLogging.logger {}

@ConfigurationProperties(prefix = "antiplagiat")
@SpringBootApplication

class AntiplagiatApplication

fun main(args: Array<String>) {
    logger.info { "Запуск Antiplagiat Application..." }
	runApplication<AntiplagiatApplication>(*args)
}
