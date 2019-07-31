/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.client.tests.utils.*
import kotlin.test.*

class FeaturesTest : ClientLoader() {
    private val TEST_URL = "$TEST_SERVER/features"

    @Test
    fun testIgnoreBodyWithPipelining() = clientTests {
        config {
            engine {
                pipelining = true
            }
        }

        test {
            ignoreBodyTestCase(it)
        }
    }

    @Test
    fun testIgnoreBodyWithoutPipelining() = clientTests {
        config {
            engine {
                pipelining = false
            }
        }

        test {
            ignoreBodyTestCase(it)
        }
    }

    private suspend fun ignoreBodyTestCase(client: HttpClient) {
        listOf(0, 1, 1024, 4 * 1024, 16 * 1024, 16 * 1024 * 1024).forEach {
            client.get<Unit>("$TEST_URL/body") {
                parameter("size", it.toString())
            }
        }
    }

    @Test
    fun bodyObserverTest() {
        var observerExecuted = false
        clientTest {
            val body = "Hello, world"
            config {
                ResponseObserver { response ->
                    val text = response.receive<String>()
                    assertEquals(body, text)
                    observerExecuted = true
                }
            }

            test { client ->
                val response = client.get<HttpResponse>("$TEST_URL/echo")
                val text = response.receive<String>()
                assertEquals(body, text)
            }

        }

        assertTrue(observerExecuted)
    }
}
