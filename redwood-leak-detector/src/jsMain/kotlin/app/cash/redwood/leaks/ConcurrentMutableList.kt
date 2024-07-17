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

@JsName("Array")
internal external class JsArray<T> {
  val length: Int
  fun at(index: Int): T?
  fun push(element: T)
  fun splice(start: Int, deleteCount: Int)
}

internal actual typealias ConcurrentMutableList<T> = JsArray<T>

internal actual inline fun <T> concurrentMutableListOf(): ConcurrentMutableList<T> {
  return JsArray()
}

internal actual inline operator fun <T> ConcurrentMutableList<T>.plusAssign(element: T) {
  push(element)
}

internal actual inline fun <T> ConcurrentMutableList<T>.removeIf(predicate: (element: T) -> Boolean) {
  var i = 0
  while (i < length) {
    // We know the index is safe and will return a T.
    if (predicate(at(i).unsafeCast<T>())) {
      splice(i, 1)
    } else {
      i++
    }
  }
}
