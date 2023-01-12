package com.example

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import com.example.RequestContext.requestId
import com.fasterxml.jackson.core.JsonGenerator
import io.micronaut.http.HttpRequest
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.runtime.Micronaut.build
import io.micronaut.runtime.Micronaut.run
import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.instrumentation.logback.v1_0.OpenTelemetryAppender
import mu.KotlinLogging
import net.logstash.logback.composite.AbstractJsonProvider
import net.logstash.logback.encoder.LogstashEncoder

fun main(args: Array<String>) {
	build().args(*args).start()
//	enableJSONLogging()
}

private fun enableJSONLogging() {
	val rootLogger = KotlinLogging.logger(Logger.ROOT_LOGGER_NAME)
	val rootLoggerImpl = rootLogger.underlyingLogger as Logger
	val consoleAppender = rootLoggerImpl.getAppender("STDOUT") as ConsoleAppender
	consoleAppender.encoder =
		LogstashEncoder().apply {
			context = rootLoggerImpl.loggerContext
			this.addProvider(DiagnosticContextProvider())
			start()
		}
}


class DiagnosticContextProvider : AbstractJsonProvider<ILoggingEvent>() {
	override fun writeTo(generator: JsonGenerator, event: ILoggingEvent) {
		ServerRequestContext.currentRequest<Any>()?.ifPresent { request ->
			request.requestId?.let { write(generator, "request_id", it) }
		}
	}

	private fun write(generator: JsonGenerator, key: String, value: String) {
		generator.writeFieldName(key)
		generator.writeObject(value)
	}
}

internal object RequestContext {
	val HttpRequest<*>.requestId: String?
		get() = this.headers.get("Request-Id")
}

