/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.engine.mock

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*

@Suppress("KDocMissingDocumentation")
@KtorExperimentalAPI
suspend fun OutgoingContent.toByteArray(): ByteArray = when (this) {
    is OutgoingContent.ByteArrayContent -> bytes()
    is OutgoingContent.ReadChannelContent -> readFrom().toByteArray()
    is OutgoingContent.WriteChannelContent -> {
        ByteChannel().also { writeTo(it) }.toByteArray()
    }
    else -> ByteArray(0)
}

@Suppress("KDocMissingDocumentation")
@KtorExperimentalAPI
suspend fun OutgoingContent.toByteReadPacket(): ByteReadPacket = when (this) {
    is OutgoingContent.ByteArrayContent -> ByteReadPacket(bytes())
    is OutgoingContent.ReadChannelContent -> readFrom().readRemaining()
    is OutgoingContent.WriteChannelContent -> {
        ByteChannel().also { writeTo(it) }.readRemaining()
    }
    else -> ByteReadPacket.Empty
}

/**
 * Send error response.
 */
fun MockRequestHandleScope.respondError(
    status: HttpStatusCode,
    content: String = status.description,
    headers: Headers = headersOf()
): HttpResponseData = respond(content, status, headers)

/**
 * Send ok response.
 */
fun MockRequestHandleScope.respondOk(
    content: String = ""
): HttpResponseData = respond(content, HttpStatusCode.OK)


/**
 * Send [HttpStatusCode.BadRequest] response.
 */
fun MockRequestHandleScope.respondBadRequest() = respond("Bad Request", HttpStatusCode.BadRequest)

/**
 * Send response with specified string [content], [status] and [headers].
 */
fun MockRequestHandleScope.respond(
    content: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    headers: Headers = headersOf()
): HttpResponseData =
    respond(ByteReadChannel(content.toByteArray(Charsets.UTF_8)), status, headers)

/**
 * Send response with specified bytes [content], [status] and [headers].
 */
fun MockRequestHandleScope.respond(
    content: ByteArray,
    status: HttpStatusCode = HttpStatusCode.OK,
    headers: Headers = headersOf()
): HttpResponseData = respond(ByteReadChannel(content), status, headers)

/**
 * Send response with specified [ByteReadChannel] [content], [status] and [headers].
 */
fun MockRequestHandleScope.respond(
    content: ByteReadChannel,
    status: HttpStatusCode = HttpStatusCode.OK,
    headers: Headers = headersOf()
): HttpResponseData = HttpResponseData(
    status, GMTDate(), headers, HttpProtocolVersion.HTTP_1_1, content, callContext
)
