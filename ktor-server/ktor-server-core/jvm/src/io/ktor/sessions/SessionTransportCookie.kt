/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package io.ktor.sessions

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.date.*

const val DEFAULT_SESSION_MAX_AGE: Long = 7L * 24 * 3600 // 7 days

/**
 * SessionTransport that adds a Set-Cookie header and reads Cookie header
 * for the specified cookie [name], and a specific cookie [configuration] after
 * applying/un-applying the specified transforms defined by [transformers].
 *
 * @property name is a cookie name
 * @property configuration is a cookie configuration
 * @property transformers is a list of session transformers
 */
class SessionTransportCookie(
    val name: String,
    val configuration: CookieConfiguration,
    val transformers: List<SessionTransportTransformer>
) : SessionTransport {

    override fun receive(call: ApplicationCall): String? {
        return transformers.transformRead(call.request.cookies[name])
    }

    override fun send(call: ApplicationCall, value: String) {
        val now = GMTDate()
        val maxAge = configuration.maxAgeInSeconds
        val expires = when {
            maxAge == 0L -> null
            else -> now + maxAge * 1000L
        }

        val cookie = Cookie(
            name,
            transformers.transformWrite(value),
            configuration.encoding,
            maxAge.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            expires,
            configuration.domain,
            configuration.path,
            configuration.secure,
            configuration.httpOnly,
            configuration.extensions
        )

        call.response.cookies.append(cookie)
    }

    override fun clear(call: ApplicationCall) {
        call.response.cookies.appendExpired(name, configuration.domain, configuration.path)
    }

    override fun toString(): String {
        return "SessionTransportCookie: $name"
    }
}

/**
 * Cookie configuration being used to send sessions
 */
class CookieConfiguration {
    /**
     * Cookie time to live duration or `null` for session cookies.
     * Session cookies are client-driven. For example, a web browser usually removes session
     * cookies at browser or window close unless the session is restored.
     */
    @Suppress("DEPRECATION", "unused")
    @Deprecated("Use maxAge or maxAgeInSeconds instead.", level = DeprecationLevel.HIDDEN)
    var duration: java.time.temporal.TemporalAmount?
        get() = duration
        set(newDuration) {
            duration = newDuration
        }

    /**
     * Cookie time to live duration or 0 for session cookies.
     * Session cookies are client-driven. For example, a web browser usually removes session
     * cookies at browser or window close unless the session is restored.
     */
    var maxAgeInSeconds: Long = DEFAULT_SESSION_MAX_AGE
        set(newMaxAge) {
            require(newMaxAge >= 0) { "maxAgeInSeconds shouldn't be negative: $newMaxAge" }
            field = newMaxAge
        }

    /**
     * Cookie encoding
     */
    var encoding: CookieEncoding = CookieEncoding.URI_ENCODING

    /**
     * Cookie domain
     */
    var domain: String? = null

    /**
     * Cookie path
     */
    var path: String? = "/"

    /**
     * Send cookies only over secure connection
     */
    var secure: Boolean = false

    /**
     * This cookie is only for transferring over HTTP(s) and shouldn't be accessible via JavaScript
     */
    var httpOnly: Boolean = true

    /**
     * Any additional extra cookie parameters
     */
    val extensions: MutableMap<String, String?> = mutableMapOf()
}
