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

@Suppress("ACTUAL_TYPE_ALIAS_NOT_TO_CLASS") // On the JVM it aliases to java.util.ArrayList.
internal actual typealias PlatformList<T> = ArrayList<T>

internal actual inline fun <T> platformListOf(): PlatformList<T> {
  return ArrayList()
}

internal actual inline val <T> PlatformList<T>.size: Int get() = size

internal actual inline operator fun <T> PlatformList<T>.get(index: Int): T {
  return get(index)
}

internal actual inline fun <T> PlatformList<T>.add(index: Int, element: T) {
  add(index, element)
}

internal actual inline fun <T> PlatformList<T>.remove(index: Int, count: Int) {
  // Force resolution to the generated extension.
  (this as MutableList<T>).remove(index, count)
}

internal actual inline fun <T> PlatformList<T>.move(from: Int, to: Int, count: Int) {
  // Force resolution to the generated extension.
  (this as MutableList<T>).move(from, to, count)
}
