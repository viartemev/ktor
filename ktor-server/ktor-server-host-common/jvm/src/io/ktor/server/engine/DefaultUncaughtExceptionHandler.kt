/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.engine

import io.ktor.util.*
import kotlinx.coroutines.*
import org.slf4j.*
import java.io.*
import kotlin.coroutines.*

/**
 * Handles all uncaught exceptions and logs errors with the specified [logger]
 * ignoring [CancellationException] and [IOException].
 */
@EngineAPI
class DefaultUncaughtExceptionHandler(
    private val logger: () -> Logger
) : CoroutineExceptionHandler {
    constructor(logger: Logger) : this({ logger })

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler.Key

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        if (exception is CancellationException) return
        if (exception is IOException) return

        logger().error(exception)
    }
}
