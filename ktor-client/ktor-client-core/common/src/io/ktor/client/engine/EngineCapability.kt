/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine

import io.ktor.client.features.*
import io.ktor.util.*
import kotlin.native.concurrent.*

@SharedImmutable
val engineCapabilitiesKey = AttributeKey<MutableMap<EngineCapability<*>, Any>>("EngineCapabilities")

@SharedImmutable
val defaultCapabilities = setOf(HttpTimeout)

interface EngineCapability<T>
