/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.util

import kotlin.native.concurrent.SharedImmutable

@InternalAPI
actual fun Platform(): Platform = NATIVE_PLATFORM

@SharedImmutable
private val NATIVE_PLATFORM = Platform("Native")
