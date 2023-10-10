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
package app.cash.redwood.lazylayout.widget

internal class SparseList<T> : AbstractList<T?>() {
  /** The non-null elements of this sparse list. */
  private val elements = mutableListOf<T & Any>()

  /**
   * This contains the external indexes of the values in [elements]. The last element is the size of
   * this sparse list.
   *
   * For example, given this list:
   *   null, null, "A", null, "B", null, null
   *
   * The values in [elements] are:
   *   ["A", "B"]
   *
   * The values in [externalIndexes] are:
   *   [2, 4, 7]
   */
  private val externalIndexes = mutableListOf(0)

  override val size: Int
    get() = externalIndexes.last()

  /** Returns a snapshot of the non-null elements in this list. */
  val nonNullElements: List<T & Any>
    get() = elements.toList()

  override fun get(index: Int): T? {
    require(index in 0 until size)
    val internalIndex = externalIndexes.binarySearch(index)
    return when {
      internalIndex < 0 -> null
      else -> elements[internalIndex]
    }
  }

  inline fun getOrCreate(index: Int, create: () -> T & Any): T & Any {
    var result = get(index)

    if (result != null) return result

    result = create()
    set(index, result)
    return result
  }

  fun removeLast(): T? {
    return removeAt(size - 1)
  }

  fun removeAt(index: Int): T? {
    require(index in 0 until size)
    val searchIndex = externalIndexes.binarySearch(index)
    return when {
      searchIndex < 0 -> {
        for (i in -1 - searchIndex until externalIndexes.size) {
          externalIndexes[i]--
        }
        null
      }
      else -> {
        externalIndexes.removeAt(searchIndex)
        for (i in searchIndex until externalIndexes.size) {
          externalIndexes[i]--
        }
        elements.removeAt(searchIndex)
      }
    }
  }

  fun add(value: T) {
    if (value != null) {
      elements += value
      externalIndexes += externalIndexes.last() + 1
    } else {
      externalIndexes[externalIndexes.size - 1]++
    }
  }

  fun add(index: Int, value: T) {
    val insertIndex = insertIndex(index)

    val shiftFrom = when {
      value != null -> {
        elements.add(insertIndex, value)
        externalIndexes.add(insertIndex, index)
        insertIndex + 1
      }
      else -> insertIndex
    }

    for (i in shiftFrom until externalIndexes.size) {
      externalIndexes[i]++
    }
  }

  fun set(index: Int, value: T) {
    removeAt(index)
    add(index, value)
  }

  fun removeRange(fromIndex: Int, toIndex: Int) {
    val delta = toIndex - fromIndex
    val fromInternalIndex = insertIndex(fromIndex)
    val toInternalIndex = insertIndex(toIndex)

    externalIndexes.subList(fromInternalIndex, toInternalIndex).clear()
    elements.subList(fromInternalIndex, toInternalIndex).clear()
    for (i in fromInternalIndex until externalIndexes.size) {
      externalIndexes[i] -= delta
    }
  }

  fun addNulls(index: Int, count: Int) {
    for (i in insertIndex(index) until externalIndexes.size) {
      externalIndexes[i] += count
    }
  }

  private fun insertIndex(index: Int): Int {
    val searchIndex = externalIndexes.binarySearch(index)
    return when {
      searchIndex >= 0 -> searchIndex
      else -> -1 - searchIndex
    }
  }

  override fun iterator(): Iterator<T?> {
    return object : AbstractIterator<T?>() {
      var nextExternalIndex = 0
      var nextInternalIndex = 0

      override fun computeNext() {
        val limit = externalIndexes[nextInternalIndex]
        when {
          // Return a null.
          nextExternalIndex < limit -> {
            setNext(null)
            nextExternalIndex++
            return
          }
          // Return a non-null element and bump the internal index.
          nextInternalIndex < elements.size -> {
            setNext(elements[nextInternalIndex])
            nextInternalIndex++
            nextExternalIndex++
          }
          else -> done()
        }
      }
    }
  }
}
