package com.team.antiplagiat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "antiplagiat")
@SpringBootApplication
@ConfigurationPropertiesScan

class AntiplagiatApplication

fun main(args: Array<String>) {
	runApplication<AntiplagiatApplication>(*args)
}
