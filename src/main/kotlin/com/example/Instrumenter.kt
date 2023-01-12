package com.example

import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Internal
import io.micronaut.core.annotation.Nullable
import io.micronaut.scheduling.instrument.Instrumentation
import io.micronaut.scheduling.instrument.InvocationInstrumenter
import io.micronaut.scheduling.instrument.InvocationInstrumenterFactory
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory
import io.micronaut.tracing.instrument.util.TracingInvocationInstrumenterFactory
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.ContextStorage
import jakarta.inject.Singleton
import org.slf4j.MDC

@Singleton
@Requires(beans = [Tracer::class])
@Internal
@Primary
class OpenTelemetryInvocationInstrumenter : TracingInvocationInstrumenterFactory, ReactiveInvocationInstrumenterFactory {
    override fun newReactiveInvocationInstrumenter(): InvocationInstrumenter? {
        return newTracingInvocationInstrumenter()
    }

    @Nullable
    override fun newTracingInvocationInstrumenter(): InvocationInstrumenter? {
        val activeContext = ContextStorage.get().current() ?: return null
        return InvocationInstrumenter {
            val activeScope = activeContext.makeCurrent()
            Instrumentation { cleanup: Boolean -> activeScope.close() }
        }
    }
}
