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
package app.cash.redwood.protocol.compose

@JsName("Array")
internal external class JsArray<E> {
  val length: Int

  @JsName("push")
  fun add(element: E)
}

internal actual typealias PlatformList<E> = JsArray<E>

internal actual inline fun <E> PlatformList<E>.asList(): List<E> {
  return JsArrayList(this)
}

internal class JsArrayList<E>(
  private val storage: JsArray<E>,
) : AbstractList<E>(), RandomAccess {
  override val size: Int get() = storage.length

  override fun get(index: Int): E {
    return storage.asDynamic()[index].unsafeCast<E>()
  }
}
