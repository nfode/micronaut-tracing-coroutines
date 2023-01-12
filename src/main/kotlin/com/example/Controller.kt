package com.example

import io.micronaut.http.HttpRequest
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.bind.binders.HttpCoroutineContextFactory
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.tracing.instrument.kotlin.CoroutineTracingDispatcher
import io.micronaut.tracing.instrument.kotlin.CoroutineTracingDispatcherContextKey
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import reactor.util.context.Context

@Controller("/")
class Controller(
    private val coroutineTracingDispatcherFactory: HttpCoroutineContextFactory<CoroutineTracingDispatcher>

) {
    private val logger = LoggerFactory.getLogger(javaClass.name)!!

    @Get
    suspend fun test() {
        MDC.put("flip","flip")
        logger.info("pre sleep")
        delay(1.seconds)
        logger.info("after sleep")
    }

    // correctly works
    @Get("/mdc")
    // slf4j correctly configured?
    suspend fun mdc() {
        MDC.put("flip","flip")
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

