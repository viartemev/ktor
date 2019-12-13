/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

@InternalAPI
actual fun Platform(): Platform = JS_PLATFORM

private val JS_PLATFORM = Platform("Js")
