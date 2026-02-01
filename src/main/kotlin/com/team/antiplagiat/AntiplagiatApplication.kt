package com.team.antiplagiat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AntiplagiatApplication

fun main(args: Array<String>) {
	runApplication<AntiplagiatApplication>(*args)
}
