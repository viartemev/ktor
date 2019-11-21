/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests

import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.tests.utils.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlin.test.*

class HttpTimeoutTest : ClientLoader() {
    @Test
    fun getTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.get<String>("$TEST_SERVER/timeout/with-delay?delay=10")
            assertEquals("Text", response)
        }
    }

    @Test
    fun getRequestTimeoutTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 10 }
        }

        test { client ->
            assertFails {
                client.get<String>("$TEST_SERVER/timeout/with-delay?delay=5000")
            }
        }
    }

    @Test
    fun getRequestTimeoutPerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFails {
                client.get<String>("$TEST_SERVER/timeout/with-delay?delay=5000") {
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 10)
                    )
                }
            }
        }
    }

    @Test
    fun getWithSeparateReceiveTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.request<HttpResponse>("$TEST_SERVER/timeout/with-delay?delay=10") {
                method = HttpMethod.Get
            }
            val res: String = response.receive()

            assertEquals("Text", res)
        }
    }

    @Test
    fun getWithSeparateReceivePerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.request<HttpResponse>("$TEST_SERVER/timeout/with-delay?delay=10") {
                method = HttpMethod.Get
                setExtension(
                    HttpTimeout.HttpTimeoutExtension.key,
                    HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                )
            }
            val res: String = response.receive()

            assertEquals("Text", res)
        }
    }

    @Test
    fun getRequestTimeoutWithSeparateReceiveTest() = clientTests(listOf("Curl", "Ios", "Js")) {
        config {
            install(HttpTimeout) { requestTimeout = 1000 }
        }

        test { client ->
            val response = client.request<ByteReadChannel>("$TEST_SERVER/timeout/with-stream?delay=500") {
                method = HttpMethod.Get
            }
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                response.readUTF8Line()
            }
        }
    }

    @Test
    fun getRequestTimeoutWithSeparateReceivePerRequestAttributesTest() = clientTests(listOf("Curl", "Ios", "Js")) {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.request<ByteReadChannel>("$TEST_SERVER/timeout/with-stream?delay=500") {
                method = HttpMethod.Get
                setExtension(
                    HttpTimeout.HttpTimeoutExtension.key,
                    HttpTimeout.HttpTimeoutExtension(requestTimeout = 1000)
                )
            }
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                response.readUTF8Line()
            }
        }
    }

    @Test
    fun getStreamTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.get<ByteArray>("$TEST_SERVER/timeout/with-stream?delay=10")

            assertEquals("Text", String(response))
        }
    }

    @Test
    fun getStreamPerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.get<ByteArray>("$TEST_SERVER/timeout/with-stream?delay=10") {
                setExtension(
                    HttpTimeout.HttpTimeoutExtension.key,
                    HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                )
            }

            assertEquals("Text", String(response))
        }
    }

    @Test
    fun getStreamRequestTimeoutTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<ByteArray>("$TEST_SERVER/timeout/with-stream?delay=200")
            }
        }
    }

    @Test
    fun getStreamRequestTimeoutPerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<ByteArray>("$TEST_SERVER/timeout/with-stream?delay=200") {
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                    )
                }
            }
        }
    }

    @Test
    fun redirectTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.get<String>("$TEST_SERVER/timeout/with-redirect?delay=10&count=2")
            assertEquals("Text", response)
        }
    }

    @Test
    fun redirectPerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.get<String>("$TEST_SERVER/timeout/with-redirect?delay=10&count=2") {
                setExtension(
                    HttpTimeout.HttpTimeoutExtension.key,
                    HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                )
            }
            assertEquals("Text", response)
        }
    }

    @Test
    fun redirectRequestTimeoutOnFirstStepTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 10 }
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect?delay=500&count=5")
            }
        }
    }

    @Test
    fun redirectRequestTimeoutOnFirstStepPerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect?delay=500&count=5") {
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 10)
                    )
                }
            }
        }
    }

    @Test
    fun redirectRequestTimeoutOnSecondStepTest() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 200 }
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect?delay=250&count=5")
            }
        }
    }

    @Test
    fun redirectRequestTimeoutOnSecondStepPerRequestAttributesTest() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect?delay=250&count=5") {
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 200)
                    )
                }
            }
        }
    }

    @Test
    fun connectTimeoutTest() = clientTests(listOf("Js", "Ios")) {
        config {
            install(HttpTimeout) { connectTimeout = 1000 }
        }

        test { client ->
            assertFailsWithRootCause<HttpConnectTimeoutException> {
                client.get<String>("http://www.google.com:81")
            }
        }
    }

    @Test
    fun connectTimeoutPerRequestAttributesTest() = clientTests(listOf("Js", "Ios")) {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpConnectTimeoutException> {
                client.get<String>("http://www.google.com:81") {
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(connectTimeout = 1000)
                    )
                }
            }
        }
    }

    @Test
    fun socketTimeoutReadTest() = clientTests(listOf("Js", "Curl")) {
        config {
            install(HttpTimeout) { socketTimeout = 1000 }
        }

        test { client ->
            assertFailsWithRootCause<HttpSocketTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-stream?delay=5000")
            }
        }
    }

    @Test
    fun socketTimeoutReadPerRequestAttributesTest() = clientTests(listOf("Js", "Curl")) {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpSocketTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-stream?delay=5000") {
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(socketTimeout = 1000)
                    )
                }
            }
        }
    }

    @Test
    fun socketTimeoutWriteFailOnWriteTest() = clientTests(listOf("Js", "Curl", "Android")) {
        config {
            install(HttpTimeout) { socketTimeout = 500 }
        }

        test { client ->
            assertFailsWithRootCause<HttpSocketTimeoutException> {
                client.post("$TEST_SERVER/timeout/slow-read") { body = makeString(4 * 1024 * 1024) }
            }
        }
    }

    @Test
    fun socketTimeoutWriteFailOnWritePerRequestAttributesTest() = clientTests(listOf("Js", "Curl", "Android")) {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpSocketTimeoutException> {
                client.post("$TEST_SERVER/timeout/slow-read") {
                    body = makeString(4 * 1024 * 1024)
                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(socketTimeout = 500)
                    )
                }
            }
        }
    }
}
