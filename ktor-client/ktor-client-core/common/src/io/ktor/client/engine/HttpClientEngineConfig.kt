/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine

import io.ktor.client.*
import io.ktor.client.response.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*

/**
 * Base configuration for [HttpClientEngine].
 */
@HttpClientDsl
open class HttpClientEngineConfig {
    /**
     * Network threads count advice.
     */
    @KtorExperimentalAPI
    var threadsCount: Int = 4

    /**
     * Enable http pipelining advice.
     */
    @KtorExperimentalAPI
    var pipelining: Boolean = false

    /**
     * Proxy address to use. Use system proxy by default.
     *
     * See [ProxyBuilder] to create proxy.
     */
    @KtorExperimentalAPI
    var proxy: ProxyConfig? = null

    @Deprecated(
        "Response config is deprecated. See [HttpPlainText] feature for charset configuration",
        level = DeprecationLevel.ERROR
    )
    val response: Nothing get() =
        error("Unbound [HttpClientCall] is deprecated. Consider using [request<HttpResponse>(block)] in instead.")
}
