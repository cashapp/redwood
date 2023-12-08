/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.redwood.treehouse

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler

/**
 * This installs a global coroutine exception handler using an internal API.
 *
 * This is necessary for uncaught exceptions to be delivered to the host application. Otherwise
 * they're logged to the console where they're easily missed.
 *
 * This assumes we're the [StandardAppLifecycle] owns the entire JS runtime.
 */
@Suppress("INVISIBLE_MEMBER")
internal actual fun prepareEnvironment(
  coroutineExceptionHandler: CoroutineExceptionHandler,
) {
  kotlinx.coroutines.internal.ensurePlatformExceptionHandlerLoaded(
    object : CoroutineExceptionHandler by coroutineExceptionHandler {
      override fun handleException(context: CoroutineContext, exception: Throwable) {
        coroutineExceptionHandler.handleException(context, exception)
        throw kotlinx.coroutines.internal.ExceptionSuccessfullyProcessed
      }
    },
  )
}
