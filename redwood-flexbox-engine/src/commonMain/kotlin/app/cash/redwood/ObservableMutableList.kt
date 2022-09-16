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
package app.cash.redwood

internal class ObservableMutableList<T>(
  private val delegate: MutableList<T> = mutableListOf(),
  private val onChange: () -> Unit = {},
  // Delegate to List-only so new mutable methods have to be implemented manually.
) : MutableList<T>, List<T> by delegate {

  override fun add(element: T): Boolean {
    return delegate.add(element).also { onChange() }
  }

  override fun add(index: Int, element: T) {
    delegate.add(index, element).also { onChange() }
  }

  override fun set(index: Int, element: T): T {
    return delegate.set(index, element).also { onChange() }
  }

  override fun addAll(elements: Collection<T>): Boolean {
    return delegate.addAll(elements).also { onChange() }
  }

  override fun addAll(index: Int, elements: Collection<T>): Boolean {
    return delegate.addAll(index, elements).also { onChange() }
  }

  override fun remove(element: T): Boolean {
    return delegate.remove(element).also { onChange() }
  }

  override fun removeAt(index: Int): T {
    return delegate.removeAt(index).also { onChange() }
  }

  override fun removeAll(elements: Collection<T>): Boolean {
    return delegate.removeAll(elements).also { onChange() }
  }

  override fun retainAll(elements: Collection<T>): Boolean {
    return delegate.retainAll(elements).also { onChange() }
  }

  override fun clear() {
    delegate.clear().also { onChange() }
  }

  override fun iterator() = delegate.iterator()

  override fun listIterator() = delegate.listIterator()

  override fun listIterator(index: Int) = delegate.listIterator(index)

  override fun subList(fromIndex: Int, toIndex: Int) = delegate.subList(fromIndex, toIndex)
}
