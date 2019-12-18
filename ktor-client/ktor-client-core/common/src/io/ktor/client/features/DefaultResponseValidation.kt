/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.util.*
import io.ktor.client.statement.*
import kotlin.jvm.*
import kotlin.native.concurrent.*

@SharedImmutable
private val ValidateMark = AttributeKey<Unit>("ValidateMark")

/**
 * Default response validation.
 * Check the response status code in range (0..299).
 */
fun HttpClientConfig<*>.addDefaultResponseValidation() {
    HttpResponseValidator {
        validateResponse { response ->
            val statusCode = response.status.value
            val originCall = response.call
            if (statusCode < 300 || originCall.attributes.contains(ValidateMark)) return@validateResponse

            val exceptionCall = originCall.save().apply {
                attributes.put(ValidateMark, Unit)
            }

            val exceptionResponse = exceptionCall.response
            when (statusCode) {
                in 300..399 -> throw RedirectResponseException(exceptionResponse)
                in 400..499 -> throw ClientRequestException(exceptionResponse)
                in 500..599 -> throw ServerResponseException(exceptionResponse)
                else -> throw ResponseException(exceptionResponse)
            }
        }
    }
}

/**
 * Base for default response exceptions.
 * @param response: origin response
 */
open class ResponseException(
    @Transient val response: HttpResponse
) : IllegalStateException("Bad response: $response")

/**
 * Unhandled redirect exception.
 */
@Suppress("KDocMissingDocumentation")
class RedirectResponseException(response: HttpResponse) : ResponseException(response) {
    override val message: String? = "Unhandled redirect: ${response.call.request.url}. Status: ${response.status}"
}

/**
 * Server error exception.
 */
@Suppress("KDocMissingDocumentation")
class ServerResponseException(
    response: HttpResponse
) : ResponseException(response) {
    override val message: String? = "Server error(${response.call.request.url}: ${response.status}."
}

/**
 * Bad client request exception.
 */
@Suppress("KDocMissingDocumentation")
class ClientRequestException(
    response: HttpResponse
) : ResponseException(response) {
    override val message: String? = "Client request(${response.call.request.url}) invalid: ${response.status}"
}
