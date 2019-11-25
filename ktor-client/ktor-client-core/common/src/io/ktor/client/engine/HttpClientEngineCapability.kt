/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine

import io.ktor.client.features.*
import io.ktor.util.*
import kotlin.native.concurrent.*

/**
 * Key required to access capabilities.
 */
@KtorExperimentalAPI
@SharedImmutable
val engineCapabilitiesKey = AttributeKey<MutableMap<HttpClientEngineCapability<*>, Any>>("EngineCapabilities")

/**
 * Default capabilities expected to be supported by engine.
 */
@KtorExperimentalAPI
@SharedImmutable
val defaultCapabilities = setOf(HttpTimeout)

/**
 * Capability required by request to be supported by [HttpClientEngine].
 */
interface HttpClientEngineCapability<T> {
    /**
     * Create an empty instance of capability configuration.
     */
    fun createEmptyConfiguration(): T
}
