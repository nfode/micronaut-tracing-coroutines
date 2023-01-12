package com.example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

@Controller("/")
class Controller {
    private val logger = LoggerFactory.getLogger(javaClass.name)!!

    @Get
    suspend fun suspending() {
        logger.info("pre sleep")
        delay(1.seconds)
        logger.info("after sleep")
    }

    @Get("/block")
    fun blocking() = runBlocking {
        logger.info("block - pre sleep")
        delay(1.seconds)
        logger.info("block - after sleep")
    }

}

