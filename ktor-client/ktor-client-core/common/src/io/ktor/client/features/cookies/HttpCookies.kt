/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features.cookies

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.io.core.*

/**
 * [HttpClient] feature that handles sent `Cookie`, and received `Set-Cookie` headers,
 * using a specific [storage] for storing and retrieving cookies.
 *
 * You can configure the [Config.storage] and to provide [Config.default] blocks to set
 * cookies when installing.
 */
class HttpCookies(
    private val storage: CookiesStorage,
    private val defaults: List<Pair<Url, Cookie>>
) : Closeable {
    private lateinit var init: Job

    /**
     * Find all cookies by [requestUrl].
     */
    suspend fun get(requestUrl: Url): List<Cookie> {
        init.join()
        return storage.get(requestUrl)
    }

    suspend fun addCookie(url: Url, cookie: Cookie) {
        init.join()
        storage.addCookie(url, cookie)
    }

    override fun close() {
        storage.close()
    }

    class Config {
        private val defaultConfig = mutableListOf<CookiesStorage.() -> Unit>()

        /**
         * [CookiesStorage] that will be used at this feature.
         * By default it just uses an initially empty in-memory [AcceptAllCookiesStorage].
         */
        var storage: CookiesStorage? = null

        /**
         * List of default cookies.
         */
        val defaultCookies: MutableList<Pair<Url, Cookie>> = mutableListOf()

        /**
         * Registers a [block] that will be called when the configuration is complete the specified [storage].
         */
        fun default(block: CookiesStorage.() -> Unit) {
            defaultConfig.add(block)
        }

        /**
         * Setup default cookies by calling [CookiesStorage.addCookie].
         */
        fun default(vararg cookies: Pair<String, Cookie>) {
            val default = cookies.map { (urlString, cookie) ->
                Url(urlString) to cookie
            }
            defaultCookies += default
        }

        internal fun build(): HttpCookies {
            val storage = storage ?: AcceptAllCookiesStorage()

            defaultConfig.forEach {
                it(storage)
            }

            return HttpCookies(storage, defaultCookies)
        }
    }

    companion object : HttpClientFeature<Config, HttpCookies> {
        override fun prepare(block: Config.() -> Unit): HttpCookies = Config().apply(block).build()

        override val key: AttributeKey<HttpCookies> = AttributeKey("HttpCookies")

        override fun install(feature: HttpCookies, scope: HttpClient) {
            feature.init = scope.launch {
                feature.defaults.forEach { (url, cookie) ->
                    feature.storage.addCookie(url, cookie)
                }
            }

            scope.sendPipeline.intercept(HttpSendPipeline.State) {
                val cookies = feature.get(context.url.clone().build())
                with(context) {
                    headers[HttpHeaders.Cookie] = renderClientCookies(cookies)
                }
            }

            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                val url = context.request.url
                response.setCookie().forEach {
                    feature.addCookie(url, it)
                }
            }
        }
    }
}

private fun renderClientCookies(cookies: List<Cookie>): String = buildString {
    cookies.forEach {
        append(it.name)
        append('=')
        append(encodeCookieValue(it.value, CookieEncoding.DQUOTES))
        append(';')
    }
}

/**
 * Gets all the cookies for the specified [url] for this [HttpClient].
 */
suspend fun HttpClient.cookies(url: Url): List<Cookie> = feature(HttpCookies)?.get(url) ?: emptyList()

/**
 * Gets all the cookies for the specified [urlString] for this [HttpClient].
 */
suspend fun HttpClient.cookies(urlString: String): List<Cookie> =
    feature(HttpCookies)?.get(Url(urlString)) ?: emptyList()

/**
 * Find the [Cookie] by [name]
 */
operator fun List<Cookie>.get(name: String): Cookie? = find { it.name == name }
