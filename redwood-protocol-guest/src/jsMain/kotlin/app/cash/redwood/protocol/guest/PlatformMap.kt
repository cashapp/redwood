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
package app.cash.redwood.protocol.guest

@JsName("Map")
internal external class JsMap<K, V> {
  operator fun get(key: K): V?
  fun set(key: K, value: V)
  fun has(key: K): Boolean
  fun delete(key: K)
}

internal actual typealias PlatformMap<K, V> = JsMap<K, V>

internal actual inline operator fun <K, V> PlatformMap<K, V>.set(key: K, value: V) {
  set(key, value)
}

internal actual inline operator fun <K, V> PlatformMap<K, V>.contains(key: K): Boolean {
  return has(key)
}

internal actual inline fun <K, V> PlatformMap<K, V>.remove(key: K) {
  delete(key)
}
