/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.redwood.flexbox

internal class ObservableMutableList<T>(
  private val onChange: () -> Unit = {},
) : AbstractMutableList<T>() {

  private val delegate = mutableListOf<T>()

  override val size: Int get() = delegate.size

  override fun get(index: Int): T {
    return delegate[index]
  }

  override fun add(index: Int, element: T) {
    delegate.add(index, element).also { onChange() }
  }

  override fun set(index: Int, element: T): T {
    return delegate.set(index, element).also { onChange() }
  }

  override fun removeAt(index: Int): T {
    return delegate.removeAt(index).also { onChange() }
  }
}
