/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.js

import io.ktor.client.engine.*
import org.w3c.fetch.*

/**
 * Configuration for [JS] client engine.
 */
class JsEngineConfig : HttpClientEngineConfig() {
    /**
     * Platform [RequestInit] configuration.
     */
    var requestConfig: RequestInit.() -> Unit = {}

    /**
     * Add [block] to current [requestConfig].
     */
    fun configureRequest(block: RequestInit.() -> Unit) {
        val current = requestConfig
        requestConfig = {
            current()
            block()
        }
    }
}
