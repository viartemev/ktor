/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.application.*
import io.ktor.util.cio.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.coroutines.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CancellationException
import java.nio.channels.*
import java.util.concurrent.*

/**
 * Default engine pipeline for all engines. Use it only if you are writing your own application engine implementation.
 */
@EngineAPI
fun  defaultEnginePipeline(environment: ApplicationEnvironment): EnginePipeline {
    val pipeline = EnginePipeline()

    environment.config.propertyOrNull("ktor.deployment.shutdown.url")?.getString()?.let { url ->
        pipeline.install(ShutDownUrl.EngineFeature) {
            shutDownUrl = url
        }
    }

    pipeline.intercept(EnginePipeline.Call) {
        try {
            call.application.execute(call)
            if (call.response.status() == null) {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch (error: ChannelIOException) {
            with(CallLogging.Internals) {
                withMDCBlock {
                    call.application.environment.logFailure(call, error)
                }
            }
        } catch (error: Throwable) {
            with(CallLogging.Internals) {
                withMDCBlock {
                    call.application.environment.logFailure(call, error)
                    handleFailure(error)
                }
            }
        } finally {
            try {
                call.request.receiveChannel().discard()
            } catch (ignore: Throwable) {
            }
        }
    }

    return pipeline
}

/**
 * Map [cause] to the corresponding status code or `null` if no default exception mapping for this [cause] type
 */
@EngineAPI
@KtorExperimentalAPI
fun defaultExceptionStatusCode(cause: Throwable): HttpStatusCode? {
    return when (cause) {
        is BadRequestException -> HttpStatusCode.BadRequest
        is NotFoundException -> HttpStatusCode.NotFound
        is UnsupportedMediaTypeException -> HttpStatusCode.UnsupportedMediaType
        is TimeoutException, is TimeoutCancellationException -> HttpStatusCode.GatewayTimeout
        else -> null
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleFailure(cause: Throwable) {
    tryRespondError(defaultExceptionStatusCode(cause) ?: HttpStatusCode.InternalServerError)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.tryRespondError(statusCode: HttpStatusCode) {
    try {
        if (call.response.status() == null) {
            call.respond(statusCode)
        }
    } catch (ignore: BaseApplicationResponse.ResponseAlreadySentException) {
    }
}

private fun ApplicationEnvironment.logFailure(call: ApplicationCall, cause: Throwable) {
    try {
        val status = call.response.status() ?: "Unhandled"
        val logString = try {
            call.request.toLogString()
        } catch (cause: Throwable) {
            "(request error: $cause)"
        }

        when (cause) {
            is CancellationException -> log.info("$status: $logString, cancelled")
            is ClosedChannelException -> log.info("$status: $logString, channel closed")
            is ChannelIOException -> log.info("$status: $logString, channel failed")
            else -> log.error("$status: $logString", cause)
        }
    } catch (oom: OutOfMemoryError) {
        try {
            log.error(cause)
        } catch (oomAttempt2: OutOfMemoryError) {
            System.err.print("OutOfMemoryError: ")
            System.err.println(cause.message)
        }
    }
}
