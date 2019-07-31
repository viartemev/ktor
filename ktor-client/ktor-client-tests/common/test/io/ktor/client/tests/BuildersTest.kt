/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests

import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.tests.utils.*
import io.ktor.http.*
import kotlin.collections.get
import kotlin.test.*

class BuildersTest : ClientLoader() {
    private val TEST_URL = "$TEST_SERVER/builders"

    @Test
    fun getEmptyResponseTest() = clientTests {
        test { client ->
            val response = client.get<String>("$TEST_URL/empty")
            assertEquals("", response)
        }
    }

    @Test
    fun testNotFound() = clientTests {
        test { client ->
            assertFailsWith<ResponseException> {
                client.get<String>("$TEST_URL/notFound")
            }
        }
    }

    @Test
    fun testDefaultRequest() = clientTests {
        test { rawClient ->

            val client = rawClient.config {
                defaultRequest {
                    url.takeFrom(TEST_URL)
                }
            }

            assertEquals("hello", client.get<String>(path = "hello"))
        }
    }
}
