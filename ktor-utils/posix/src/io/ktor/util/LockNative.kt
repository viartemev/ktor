/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

import kotlinx.cinterop.*
import io.ktor.utils.io.core.*
import utils.*
import kotlin.native.concurrent.*

@InternalAPI
actual class Lock {
    private val mutex = nativeHeap.alloc<ktor_mutex_t>()
    private val closed = AtomicInt(0)

    init {
        freeze()
        ktor_mutex_create(mutex.ptr).checkResult { "Failed to create mutex." }
    }

    actual fun lock() {
        check(closed.value == 0)

        ktor_mutex_lock(mutex.ptr).checkResult { "Failed to lock mutex." }
    }

    actual fun unlock() {
        check(closed.value == 0)

        ktor_mutex_unlock(mutex.ptr).checkResult { "Failed to unlock mutex." }
    }

    actual fun close() {
        if (!closed.compareAndSet(0, 1)) {
            return
        }
        ktor_mutex_destroy(mutex.ptr)
        nativeHeap.free(mutex)
    }
}

private inline fun Int.checkResult(block: () -> String) {
    check(this == 0, block)
}
