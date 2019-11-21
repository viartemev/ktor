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
    fun testGet() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.get<String>("$TEST_SERVER/timeout/with-delay") {
                parameter("delay", 10)
            }
            assertEquals("Text", response)
        }
    }

    @Test
    fun testGetRequestTimeout() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 10 }
        }

        test { client ->
            assertFails {
                client.get<String>("$TEST_SERVER/timeout/with-delay") {
                    parameter("delay", 5000)
                }
            }
        }
    }

    @Test
    fun testGetRequestTimeoutPerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFails {
                client.get<String>("$TEST_SERVER/timeout/with-delay") {
                    parameter("delay", 5000)

                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 10)
                    )
                }
            }
        }
    }

    @Test
    fun testGetWithSeparateReceive() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.request<HttpResponse>("$TEST_SERVER/timeout/with-delay") {
                method = HttpMethod.Get
                parameter("delay", 10)
            }
            val res: String = response.receive()

            assertEquals("Text", res)
        }
    }

    @Test
    fun testGetWithSeparateReceivePerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.request<HttpResponse>("$TEST_SERVER/timeout/with-delay") {
                method = HttpMethod.Get
                parameter("delay", 10)

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
    fun testGetRequestTimeoutWithSeparateReceive() = clientTests(listOf("Curl", "Ios", "Js")) {
        config {
            install(HttpTimeout) { requestTimeout = 1000 }
        }

        test { client ->
            val response = client.request<ByteReadChannel>("$TEST_SERVER/timeout/with-stream") {
                method = HttpMethod.Get
                parameter("delay", 500)
            }
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                response.readUTF8Line()
            }
        }
    }

    @Test
    fun testGetRequestTimeoutWithSeparateReceivePerRequestAttributes() = clientTests(listOf("Curl", "Ios", "Js")) {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.request<ByteReadChannel>("$TEST_SERVER/timeout/with-stream") {
                method = HttpMethod.Get
                parameter("delay", 500)

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
    fun testGetStream() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.get<ByteArray>("$TEST_SERVER/timeout/with-stream") {
                parameter("delay", 10)
            }

            assertEquals("Text", String(response))
        }
    }

    @Test
    fun testGetStreamPerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.get<ByteArray>("$TEST_SERVER/timeout/with-stream") {
                parameter("delay", 10)

                setExtension(
                    HttpTimeout.HttpTimeoutExtension.key,
                    HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                )
            }

            assertEquals("Text", String(response))
        }
    }

    @Test
    fun testGetStreamRequestTimeout() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<ByteArray>("$TEST_SERVER/timeout/with-stream") {
                    parameter("delay", 200)
                }
            }
        }
    }

    @Test
    fun testGetStreamRequestTimeoutPerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<ByteArray>("$TEST_SERVER/timeout/with-stream") {
                    parameter("delay", 200)

                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                    )
                }
            }
        }
    }

    @Test
    fun testRedirect() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 500 }
        }

        test { client ->
            val response = client.get<String>("$TEST_SERVER/timeout/with-redirect") {
                parameter("delay", 10)
                parameter("count", 2)
            }

            assertEquals("Text", response)
        }
    }

    @Test
    fun testRedirectPerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            val response = client.get<String>("$TEST_SERVER/timeout/with-redirect") {
                parameter("delay", 10)
                parameter("count", 2)

                setExtension(
                    HttpTimeout.HttpTimeoutExtension.key,
                    HttpTimeout.HttpTimeoutExtension(requestTimeout = 500)
                )
            }
            assertEquals("Text", response)
        }
    }

    @Test
    fun testRedirectRequestTimeoutOnFirstStep() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 10 }
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect") {
                    parameter("delay", 500)
                    parameter("count", 5)
                }
            }
        }
    }

    @Test
    fun testRedirectRequestTimeoutOnFirstStepPerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect") {
                    parameter("delay", 500)
                    parameter("count", 5)

                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 10)
                    )
                }
            }
        }
    }

    @Test
    fun testRedirectRequestTimeoutOnSecondStep() = clientTests {
        config {
            install(HttpTimeout) { requestTimeout = 200 }
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect") {
                    parameter("delay", 250)
                    parameter("count", 5)
                }
            }
        }
    }

    @Test
    fun testRedirectRequestTimeoutOnSecondStepPerRequestAttributes() = clientTests {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpRequestTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-redirect") {
                    parameter("delay", 250)
                    parameter("count", 5)

                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(requestTimeout = 200)
                    )
                }
            }
        }
    }

    @Test
    fun testConnectTimeout() = clientTests(listOf("Js", "Ios")) {
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
    fun testConnectTimeoutPerRequestAttributes() = clientTests(listOf("Js", "Ios")) {
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
    fun testSocketTimeoutRead() = clientTests(listOf("Js", "Curl")) {
        config {
            install(HttpTimeout) { socketTimeout = 1000 }
        }

        test { client ->
            assertFailsWithRootCause<HttpSocketTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-stream") {
                    parameter("delay", 5000)
                }
            }
        }
    }

    @Test
    fun testSocketTimeoutReadPerRequestAttributes() = clientTests(listOf("Js", "Curl")) {
        config {
            install(HttpTimeout)
        }

        test { client ->
            assertFailsWithRootCause<HttpSocketTimeoutException> {
                client.get<String>("$TEST_SERVER/timeout/with-stream") {
                    parameter("delay", 5000)

                    setExtension(
                        HttpTimeout.HttpTimeoutExtension.key,
                        HttpTimeout.HttpTimeoutExtension(socketTimeout = 1000)
                    )
                }
            }
        }
    }

    @Test
    fun testSocketTimeoutWriteFailOnWrite() = clientTests(listOf("Js", "Curl", "Android")) {
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
    fun testSocketTimeoutWriteFailOnWritePerRequestAttributes() = clientTests(listOf("Js", "Curl", "Android")) {
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
