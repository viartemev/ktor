/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlin.test.*

class CIONative {
    @Test
    fun testGoogle(): Unit {
        println("X")
        runBlocking {
            val client = HttpClient(CIO)
            val response = client.get<String>("http://www.google.ru/")
            println(response)
        }
    }
}
