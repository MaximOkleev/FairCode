package com.team.antiplagiat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
@ConfigurationProperties(prefix = "antiplagiat")
@SpringBootApplication
@ConfigurationPropertiesScan

class AntiplagiatApplication

fun main(args: Array<String>) {
    logger.info { "Запуск Antiplagiat Application..." }
	runApplication<AntiplagiatApplication>(*args)
    logger.info { "Приложение остановлено" }
}
