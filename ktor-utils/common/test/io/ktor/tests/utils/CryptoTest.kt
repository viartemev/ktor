/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.tests.utils

import io.ktor.util.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlin.test.*

class CryptoTest {
    @Test
    fun testBase64() {
        assertEquals("AAAA", ByteArray(3).encodeBase64())
        assertArrayEquals(ByteArray(3), "AAAA".decodeBase64Bytes())
    }

    @Test
    fun testHex() {
        assertEquals("00af", hex(byteArrayOf(0, 0xaf.toByte())))
        assertArrayEquals(byteArrayOf(0, 0xaf.toByte()), fromHex("00af"))
    }

    @Test
    fun testRaw() {
        assertArrayEquals(byteArrayOf(0x31, 0x32, 0x33), raw("123"))
    }

    private fun raw(string: String): ByteArray = string.toByteArray(Charsets.UTF_8)

    private fun assertArrayEquals(expected: ByteArray, actual: ByteArray) {
        assertTrue { expected.contentEquals(actual) }
    }
}
