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

internal actual typealias ConcurrentMutableList<T> = ArrayList<T>

internal actual inline fun <T> concurrentMutableListOf(): ConcurrentMutableList<T> {
  return arrayListOf()
}

internal actual inline operator fun <T> ConcurrentMutableList<T>.plusAssign(element: T) {
  add(element)
}

internal actual inline fun <T> ConcurrentMutableList<T>.removeIf(predicate: (element: T) -> Boolean) {
  var i = 0
  while (i < size) {
    if (predicate(get(i))) {
      removeAt(i)
    } else {
      i++
    }
  }
}
