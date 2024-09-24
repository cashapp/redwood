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

package app.cash.redwood.compose

@JsName("Array")
internal external class JsArray<T> {
  val length: Int
  fun splice(start: Int, deleteCount: Int): JsArray<T>
  fun splice(start: Int, deleteCount: Int, element: T)
}

internal actual typealias PlatformList<T> = JsArray<T>

internal actual inline fun <T> platformListOf(): PlatformList<T> {
  return js("[]").unsafeCast<PlatformList<T>>()
}

internal actual inline val <T> PlatformList<T>.size: Int get() = length

internal actual inline operator fun <T> PlatformList<T>.get(index: Int): T {
  return asDynamic()[index]
}

internal actual inline fun <T> PlatformList<T>.add(index: Int, element: T) {
  splice(index, 0, element)
}

internal actual inline fun <T> PlatformList<T>.remove(index: Int, count: Int) {
  splice(index, count)
}

internal actual fun <T> PlatformList<T>.move(from: Int, to: Int, count: Int) {
  val dest = if (from > to) to else to - count
  val removed = splice(from, count)
  repeat(count) { offset ->
    splice(dest + offset, 0, removed[offset])
  }
}
