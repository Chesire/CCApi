package com.chesire.capi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CapiApplication

fun main(args: Array<String>) {
    runApplication<CapiApplication>(*args)
}
