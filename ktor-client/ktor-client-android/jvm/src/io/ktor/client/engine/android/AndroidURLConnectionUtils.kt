/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.android

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import java.io.*
import java.net.*
import kotlin.coroutines.*

/**
 * Setup [HttpURLConnection] timeout configuration using [HttpTimeout.Extension] as a source.
 */
internal fun HttpURLConnection.setupTimeoutAttributes(requestData: HttpRequestData) {
    requestData.getExtensionOrNull(HttpTimeout.Extension.key)?.let { timeoutAttributes ->
        timeoutAttributes.connectTimeout?.let { connectTimeout = it.toInt() }
        timeoutAttributes.socketTimeout?.let { readTimeout = it.toInt() }
        setupRequestTimeoutAttributes(timeoutAttributes)
    }
}

/**
 * Update [HttpURLConnection] timeout configuration to support request timeout. Required to support blocking
 * [HttpURLConnection.connect] call.
 */
private fun HttpURLConnection.setupRequestTimeoutAttributes(timeoutAttributes: HttpTimeout.Extension) {
    // Android performs blocking connect call, so we need to add an upper bound on the call time.
    timeoutAttributes.requestTimeout?.let {
        if (it == 0L) return@let
        connectTimeout = when {
            connectTimeout == 0 -> it.toInt()
            connectTimeout < it -> connectTimeout
            else -> it.toInt()
        }
    }
}

/**
 * Call [HttpURLConnection.connect] catching [SocketTimeoutException] and returning [HttpSocketTimeoutException] instead
 * of it. If request timeout happens earlier [HttpRequestTimeoutException] will be thrown.
 */
internal suspend fun HttpURLConnection.timeoutAwareConnect() {
    try {
        connect()
    } catch (cause: Throwable) {
        // Allow to throw request timeout cancellation exception instead of connect timeout exception if needed.
        yield()
        throw when (cause) {
            is SocketTimeoutException -> HttpConnectTimeoutException()
            else -> cause
        }
    }
}

/**
 * Establish connection and return correspondent [ByteReadChannel].
 */
internal fun HttpURLConnection.content(callContext: CoroutineContext): ByteReadChannel = try {
    inputStream?.buffered()
} catch (_: IOException) {
    errorStream?.buffered()
}?.toByteReadChannel(
    context = callContext,
    pool = KtorDefaultPool
)?.let { CoroutineScope(callContext).mapEngineExceptions(it) } ?: ByteReadChannel.Empty
