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
package app.cash.redwood.widget.compose

internal fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int, count: Int) {
  val dest = if (fromIndex > toIndex) toIndex else toIndex - count
  if (count == 1) {
    if (fromIndex == toIndex + 1 || fromIndex == toIndex - 1) {
      // Adjacent elements, perform swap to avoid backing array manipulations.
      val fromEl = get(fromIndex)
      val toEl = set(toIndex, fromEl)
      set(fromIndex, toEl)
    } else {
      val fromEl = removeAt(fromIndex)
      add(dest, fromEl)
    }
  } else {
    val subView = subList(fromIndex, fromIndex + count)
    val subCopy = subView.toMutableList()
    subView.clear()
    addAll(dest, subCopy)
  }
}

internal fun <T> MutableList<T>.remove(index: Int, count: Int) {
  if (count == 1) {
    removeAt(index)
  } else {
    subList(index, index + count).clear()
  }
}
