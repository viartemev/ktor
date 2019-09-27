/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.server.testing.client

import io.ktor.client.engine.*
import io.ktor.server.testing.*

/**
 * A [TestHttpClientConfig] connected to the application engine.
 */
val TestApplicationEngine.TestHttpClient: HttpClientEngineFactory<TestHttpClientConfig>
    get() = TestHttpClient {}

/**
 * Configures a [TestHttpClientConfig] connected to the application engine applying [block] function.
 */
@Suppress("FunctionName")
fun TestApplicationEngine.TestHttpClient(block: TestHttpClientConfig.() -> Unit): HttpClientEngineFactory<TestHttpClientConfig> {
    return TestHttpClientEngine.config {
        app = this@TestHttpClient
        block()
    }
}
