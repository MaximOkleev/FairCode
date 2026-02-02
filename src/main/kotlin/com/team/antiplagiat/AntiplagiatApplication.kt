package com.team.antiplagiat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AntiplagiatApplication

fun main(args: Array<String>) {
	runApplication<AntiplagiatApplication>(*args)
}
