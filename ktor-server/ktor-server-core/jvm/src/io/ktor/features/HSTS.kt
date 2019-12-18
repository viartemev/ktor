/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */
package io.ktor.features

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*

/**
 * HSTS feature that appends `Strict-Transport-Security` HTTP header to every response.
 * See http://ktor.io/servers/features/hsts.html for details
 * See RFC 6797 https://tools.ietf.org/html/rfc6797
 */
class HSTS(config: Configuration) {
    /**
     * HSTS configuration
     */
    class Configuration {
        /**
         * Consents that the policy allows including the domain into web browser preloading list
         */
        var preload: Boolean = false

        /**
         * Adds includeSubDomains directive, which applies this policy to this domain and any subdomains
         */
        var includeSubDomains: Boolean = true

        /**
         * Duration to tell the client to keep the host in a list of known HSTS hosts
         */
        @Suppress("unused", "DEPRECATION")
        @Deprecated("Use maxAgeInSeconds or maxAgeDuration instead.", level = DeprecationLevel.HIDDEN)
        var maxAge: java.time.Duration
            get() = maxAge
            set(newDuration) {
                maxAge = newDuration
            }

        /**
         * Duration in seconds to tell the client to keep the host in a list of known HSTS hosts.
         */
        var maxAgeInSeconds: Long = DEFAULT_HSTS_MAX_AGE
            set(newMaxAge) {
                check(newMaxAge >= 0L) { "maxAgeInSeconds shouldn't be negative: $newMaxAge" }
                field = newMaxAge
            }

        /**
         * Any custom directives supported by specific user-agent
         */
        val customDirectives: MutableMap<String, String?> = HashMap()
    }

    /**
     * Constructed `Strict-Transport-Security` header value
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val headerValue: String = buildString {
        append("max-age=")
        append(config.maxAgeInSeconds)

        if (config.includeSubDomains) {
            append("; includeSubDomains")
        }
        if (config.preload) {
            append("; preload")
        }

        if (config.customDirectives.isNotEmpty()) {
            config.customDirectives.entries.joinTo(this, separator = "; ", prefix = "; ") {
                if (it.value != null) {
                    "${it.key.escapeIfNeeded()}=${it.value?.escapeIfNeeded()}"
                } else {
                    it.key.escapeIfNeeded()
                }
            }
        }
    }

    /**
     * Feature's main interceptor, usually installed by the feature itself
     */
    fun intercept(call: ApplicationCall) {
        if (call.request.origin.run { scheme == "https" && port == 443 }) {
            call.response.header(HttpHeaders.StrictTransportSecurity, headerValue)
        }
    }

    /**
     * Feature installation object
     */
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, HSTS> {
        const val DEFAULT_HSTS_MAX_AGE: Long = 365L * 24 * 3600 // 365 days

        override val key: AttributeKey<HSTS> = AttributeKey("HSTS")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): HSTS {
            val feature = HSTS(Configuration().apply(configure))
            pipeline.intercept(ApplicationCallPipeline.Features) { feature.intercept(call) }
            return feature
        }
    }
}
