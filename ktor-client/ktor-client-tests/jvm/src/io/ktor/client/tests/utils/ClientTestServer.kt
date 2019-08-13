/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.tests.utils

import io.ktor.application.*
import io.ktor.client.tests.utils.tests.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*

internal fun Application.tests() {
    install(WebSockets)

    authTestServer()
    buildersTest()
    cacheTestServer()
    contentTestServer()
    cookiesTest()
    encodingTestServer()
    featuresTest()
    fullFormTest()
    loggingTestServer()
    redirectTest()
    serializationTestServer()

    routing {
        post("/echo") {
            val response = call.receiveText()
            call.respond(response)
        }
        get("/bytes") {
            val size = call.request.queryParameters["size"]!!.toInt()
            call.respondBytes(makeArray(size))
        }
    }
}

internal suspend fun ApplicationCall.fail(text: String): Nothing {
    respondText(text, status = HttpStatusCode(400, text))
    error(text)
}
