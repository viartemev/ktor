/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features

import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.net.*

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class HttpConnectTimeoutException : ConnectException("Connect timeout has been expired")

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual class HttpSocketTimeoutException : SocketTimeoutException("Socket timeout has been expired")

/**
 * Returns [ByteReadChannel] with [ByteChannel.close] handler that returns [HttpSocketTimeoutException] instead of
 * [SocketTimeoutException].
 */
@InternalAPI
fun CoroutineScope.mapEngineExceptions(input: ByteReadChannel): ByteReadChannel {
    val replacementChannel = ByteChannel()
    val wrapper = ByteChannelWrapper(replacementChannel)

    GlobalScope.launch {
        try {
            input.joinTo(wrapper, true)
        } catch (cause: Throwable) {
            input.cancel(cause)
        }
    }

    return replacementChannel
}

/**
 * Returns [ByteWriteChannel] with [ByteChannel.close] handler that returns [HttpSocketTimeoutException] instead of
 * [SocketTimeoutException].
 */
@InternalAPI
fun CoroutineScope.mapEngineExceptions(input: ByteWriteChannel): ByteWriteChannel {
    val replacementChannel = ByteChannel()
    val wrapper = ByteChannelWrapper(replacementChannel)

    launch {
        try {
            wrapper.joinTo(input, true)
        } catch (cause: Throwable) {
            wrapper.close(cause)
        }
    }

    return wrapper
}

private class ByteChannelWrapper(private val delegate: ByteChannel) : ByteChannel by delegate {

    override fun close(cause: Throwable?): Boolean {
        val mappedCause = when (cause?.rootCause) {
            is SocketTimeoutException -> HttpSocketTimeoutException()
            else -> cause
        }

        return delegate.close(mappedCause)
    }
}
