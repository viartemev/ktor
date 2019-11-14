/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features

import io.ktor.util.*
import kotlin.native.concurrent.*

interface HttpClientEngineExtension<T> {

    fun getExtensionConfiguration(attributes: Attributes): T?
}

fun <T : Any> Attributes.getExtension(extension: HttpClientEngineExtension<T>): T? {
    return getOrNull(Extensions.key)?.get(extension)
}

fun <T : Any> Attributes.putExtension(extension: HttpClientEngineExtension<T>, value: T) {
    computeIfAbsent(Extensions.key) { Extensions() }.put(extension, value)
}

fun Attributes.getAllExtensions(): Set<HttpClientEngineExtension<*>> {
    return getOrNull(Extensions.key)?.getAllExtensions() ?: emptySet()
}

private class Extensions {

    private val map = mutableMapOf<HttpClientEngineExtension<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(extension: HttpClientEngineExtension<T>): T? = map[extension] as T?

    fun <T : Any> put(extension: HttpClientEngineExtension<T>, value: T) {
        map[extension] = value
    }

    fun getAllExtensions(): Set<HttpClientEngineExtension<*>> = map.keys

    companion object {
        @SharedImmutable
        internal val key = AttributeKey<Extensions>("HttpClientEngineExtensions")
    }
}
