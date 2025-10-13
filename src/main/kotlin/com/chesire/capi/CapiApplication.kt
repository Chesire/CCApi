package com.chesire.capi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class CapiApplication

fun main(args: Array<String>) {
    runApplication<CapiApplication>(*args)
}
