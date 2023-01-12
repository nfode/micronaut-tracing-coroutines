package com.example

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.bind.binders.HttpCoroutineContextFactory
import io.micronaut.tracing.instrument.kotlin.CoroutineTracingDispatcher
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@Controller("/")
class Controller(
    private val coroutineTracingDispatcherFactory: HttpCoroutineContextFactory<CoroutineTracingDispatcher>

) {
    private val logger = LoggerFactory.getLogger(javaClass.name)!!

    @Get
    suspend fun test() {
        MDC.put("flip", "flip")
        logger.info("pre sleep")
        delay(1.seconds)
        logger.info("after sleep")
    }

    // correctly works
    @Get("/mdc")
    // slf4j correctly configured?
    suspend fun mdc() {
        MDC.put("flip", "flip")
        withContext(MDCContext()) {
            logger.info("mdc - pre sleep")
            delay(1.seconds)
            logger.info("mdc - after sleep")
        }
    }

    @Get("/sus")
    fun sus(requestId: String) {
        MDC.put("test", "test")
        runBlocking {
            async {
                logger.info("sus - pre sleep")
                delay(1.seconds)
                logger.info("sus - after sleep")
            }.await()
        }
    }

    @Get("/sus2")
    fun sus2() = runBlocking {
        MDC.put("what", "what")
        withContext(coroutineTracingDispatcherFactory.create()) {
            logger.info("sus2 - pre sleep")
            delay(1.seconds)
            logger.info("sus2 - after sleep")
        }
    }

}

