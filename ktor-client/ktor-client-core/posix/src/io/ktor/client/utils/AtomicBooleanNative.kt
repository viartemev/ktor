/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.utils

import kotlin.native.concurrent.*


internal actual class AtomicBoolean actual constructor(value: Boolean) {

    private val _value = AtomicReference(value)

    actual val value: Boolean
        get() = _value.value

    init {
        freeze()
    }

    actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
        return _value.compareAndSet(expect, update)
    }
}
