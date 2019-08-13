/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests.features

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.features.cookies.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.client.tests.utils.*
import io.ktor.http.*
import kotlin.test.*

class CookiesTest : ClientLoader() {
    private val TEST_URL = "$TEST_SERVER/cookies"
    private val hostname = TEST_SERVER

    @Test
    fun testCompatibility() = clientTest(MockEngine) {
        config {
            engine {
                addHandler { request ->
                    assertEquals("*/*", request.headers[HttpHeaders.Accept])
                    val rawCookies = request.headers[HttpHeaders.Cookie]!!
                    assertEquals(1, request.headers.getAll(HttpHeaders.Cookie)?.size!!)
                    assertEquals("first=\"1,2,3,4\";second=abc;", rawCookies)

                    respondOk()
                }
            }

            install(HttpCookies) {
                default(
                    "//localhost" to Cookie("first", "1,2,3,4"),
                    "http://localhost" to Cookie("second", "abc")
                )
            }
        }

        test { client ->
            client.get<HttpResponse>()
        }
    }

    @Test
    fun testAllowedCharacters() = clientTest(MockEngine) {
        config {
            engine {
                addHandler { request ->
                    assertEquals("myServer=value:value;", request.headers[HttpHeaders.Cookie])
                    respondOk()
                }
            }

            install(HttpCookies) {
                default(
                    "http://localhost" to Cookie("myServer", "value:value")
                )
            }
        }

        test { client ->
            client.get<String>()
        }
    }

    @Test
    fun testAccept() = clientTests {
        config {
            install(HttpCookies)
        }

        test { client ->
            client.get<Unit>(TEST_URL)
            client.cookies("$hostname/cookies").let {
                assertEquals(1, it.size)
                assertEquals("my-awesome-value", it["hello-cookie"]!!.value)
            }
        }
    }

    @Test
    fun testUpdate() = clientTests {
        config {
            install(HttpCookies) {
                default(
                    hostname to Cookie("id", "1", domain = "127.0.0.1")
                )
            }
        }

        test { client ->
            repeat(10) {
                val before = client.getId()
                client.get<Unit>("$TEST_URL/update-user-id")
                assertEquals(before + 1, client.getId())
                assertEquals("ktor", client.cookies(hostname)["user"]?.value)
            }
        }
    }

    @Test
    fun testExpiration() = clientTests {
        config {
            install(HttpCookies) {
                default(
                    hostname to Cookie("id", "777", domain = "127.0.0.1", path = "/")
                )
            }

            test { client ->
                assertFalse(client.cookies(hostname).isEmpty())
                client.get<Unit>("$TEST_URL/expire")
                assertTrue(client.cookies(hostname).isEmpty(), "cookie should be expired")
            }
        }
    }

    @Test
    fun testConstant() = clientTests {
        config {
            install(HttpCookies) {
                storage = ConstantCookiesStorage(Cookie("id", "1", domain = "127.0.0.1"))
            }
        }

        test { client ->
            repeat(3) {
                client.get<Unit>("$TEST_URL/update-user-id")
                assertEquals(1, client.getId())
                assertNull(client.cookies(hostname)["user"]?.value)
            }
        }
    }

    @Test
    fun testMultipleCookies() = clientTests {
        config {
            install(HttpCookies) {
                default(
                    hostname to Cookie("first", "first-cookie", domain = "127.0.0.1"),
                    hostname to Cookie("second", "second-cookie", domain = "127.0.0.1")
                )
            }
        }

        test { client ->
            val response = client.get<String>("$TEST_URL/multiple")
            assertEquals("Multiple done", response)
        }
    }

    @Test
    fun testPath() = clientTests {
        config {
            install(HttpCookies)
        }

        test { client ->
            assertEquals("OK", client.get<String>("$TEST_URL/withPath"))
            assertEquals("OK", client.get<String>("$TEST_URL/withPath/something"))
        }
    }

    @Test
    fun testWithLeadingDot() = clientTests {
        config {
            install(HttpCookies)
        }

        test { client ->
            client.get<Unit>("https://m.vk.com")
            assertTrue(client.cookies("https://.vk.com").isNotEmpty())
            assertTrue(client.cookies("https://vk.com").isNotEmpty())
            assertTrue(client.cookies("https://m.vk.com").isNotEmpty())
            assertTrue(client.cookies("https://m.vk.com").isNotEmpty())

            assertTrue(client.cookies("https://google.com").isEmpty())
        }
    }

    @Test
    fun caseSensitive() = clientTest {
        config {
            install(HttpCookies)
        }

        test { client ->
            try {
                client.get<Unit>("$TEST_URL/foo")
                client.get<Unit>("$TEST_URL/FOO")
            } catch (cause: Throwable) {
                throw cause
            }
        }
    }

    @Test
    @Ignore
    fun multipleClients() = clientTests {
        /* a -> b
         * |    |
         * c    d
         */
        test { client ->
            val a = client.config {
                install(HttpCookies) {
                    default(hostname to Cookie("id", "1"))
                }
            }
            val b = a.config {
                install(HttpCookies) {
                    default(hostname to Cookie("id", "10"))
                }
            }

            val c = a.config { }
            val d = b.config { }

            a.get<Unit>("$TEST_URL/update-user-id")

            assertEquals(2, a.getId())
            assertEquals(2, c.getId())
            assertEquals(10, b.getId())
            assertEquals(10, d.getId())

            b.get<Unit>("$TEST_URL/update-user-id")

            assertEquals(2, a.getId())
            assertEquals(2, c.getId())
            assertEquals(11, b.getId())
            assertEquals(11, d.getId())

            c.get<Unit>(path = "$TEST_URL/update-user-id")

            assertEquals(3, a.getId())
            assertEquals(3, c.getId())
            assertEquals(11, b.getId())
            assertEquals(11, d.getId())

            d.get<Unit>("$TEST_URL/update-user-id")

            assertEquals(3, a.getId())
            assertEquals(3, c.getId())
            assertEquals(12, b.getId())
            assertEquals(12, d.getId())
        }
    }

    private suspend fun HttpClient.getId(): Int {
        val cookie = cookies(hostname)
        if (cookie.isEmpty()) {
            val x = cookies(hostname)

            check(x.isEmpty())
        }


        return cookie["id"]!!.value.toInt()
    }
}
