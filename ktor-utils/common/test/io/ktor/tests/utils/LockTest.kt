/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.utils

import io.ktor.util.*
import kotlin.test.*

class LockTest {
    @Test
    fun testLockUnlock() {
        val lock = Lock()
        lock.lock()
        lock.unlock()
        lock.close()
    }

    @Test
    fun testReentrantLock() {
        val lock = Lock()
        lock.lock()
        lock.lock()

        lock.unlock()
        lock.unlock()
        lock.close()
    }
}
