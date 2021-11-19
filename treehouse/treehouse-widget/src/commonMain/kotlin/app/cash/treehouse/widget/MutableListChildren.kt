/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.widget

import kotlin.jvm.JvmOverloads

public class MutableListChildren<T : Any>
@JvmOverloads constructor(
  public val list: MutableList<T> = mutableListOf(),
) : Widget.Children<T>, Iterable<T> {
  override fun insert(index: Int, widget: T) {
    list.add(index, widget)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    /*
     * Copyright 2019 The Android Open Source Project
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
    val dest = if (fromIndex > toIndex) toIndex else toIndex - count
    if (count == 1) {
      if (fromIndex == toIndex + 1 || fromIndex == toIndex - 1) {
        // Adjacent elements, perform swap to avoid backing array manipulations.
        val fromEl = list.get(fromIndex)
        val toEl = list.set(toIndex, fromEl)
        list.set(fromIndex, toEl)
      } else {
        val fromEl = list.removeAt(fromIndex)
        list.add(dest, fromEl)
      }
    } else {
      val subView = list.subList(fromIndex, fromIndex + count)
      val subCopy = subView.toMutableList()
      subView.clear()
      list.addAll(dest, subCopy)
    }
  }

  override fun remove(index: Int, count: Int) {
    /*
     * Copyright 2019 The Android Open Source Project
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
    if (count == 1) {
      list.removeAt(index)
    } else {
      list.subList(index, index + count).clear()
    }
  }

  override fun clear() {
    list.clear()
  }

  override fun iterator(): Iterator<T> = list.iterator()
}
