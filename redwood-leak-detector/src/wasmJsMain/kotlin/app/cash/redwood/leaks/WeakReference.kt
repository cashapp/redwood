/*
 * Copyright (C) 2024 Square, Inc.
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
@file:Suppress("NOTHING_TO_INLINE")

package app.cash.redwood.leaks

internal actual inline fun hasWeakReference(): Boolean {
  return true
}

internal actual class WeakReference<T : Any>
private constructor(
  private val real: WeakRef<JsReference<T>>,
) {
  actual constructor(referred: T) : this(WeakRef(referred.toJsReference()))

  actual fun get(): T? {
    return real.deref()?.get()
  }
}

private external class WeakRef<T : JsAny>(reference: T) {
  fun deref(): T?
}
