/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js.compatibility

import io.ktor.client.engine.js.browser.*
import io.ktor.client.engine.js.node.*
import io.ktor.client.fetch.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlin.js.Promise

internal suspend fun commonFetch(
    input: String, init: RequestInit
): Response = suspendCancellableCoroutine { continuation ->
    val controller = AbortController()
    init.signal = controller.signal

    continuation.invokeOnCancellation {
        controller.abort()
    }

    val promise: Promise<Response> = if (PlatformUtils.IS_NODE) {
        jsRequire("node-fetch")(input, init)
    } else {
        fetch(input, init)
    }

    promise.then(
        onFulfilled = {
            continuation.resumeWith(Result.success(it))
        },
        onRejected = {
            continuation.resumeWith(Result.failure(Error("Fail to fetch", it)))
        }
    )
}

internal fun AbortController(): AbortController {
    return if (PlatformUtils.IS_NODE) {
        val controller = js("require('abort-controller')")
        js("new controller()")
    } else {
        js("new AbortController()")
    }
}

internal fun CoroutineScope.readBody(
    response: Response
): ByteReadChannel = if (PlatformUtils.IS_NODE) {
    readBodyNode(response)
} else {
    readBodyBrowser(response)
}


private fun jsRequire(moduleName: String): dynamic = try {
    js("require(moduleName)")
} catch (cause: dynamic) {
    throw Error("Error loading module '$moduleName': $cause")
}
