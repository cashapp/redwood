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
package app.cash.redwood.widget.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import app.cash.redwood.widget.Widget

public class ComposeWidgetChildren : Widget.Children<@Composable () -> Unit> {
  private val children = mutableStateListOf<@Composable () -> Unit>()

  @Composable
  public fun render() {
    for (child in children) {
      child()
    }
  }

  override fun insert(index: Int, widget: @Composable () -> Unit) {
    children.add(index, widget)
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
      val element = children.removeAt(fromIndex)
      children.add(dest, element)
    } else {
      val subView = children.subList(fromIndex, fromIndex + count)
      val subCopy = subView.toMutableList()
      subView.clear()
      children.addAll(dest, subCopy)
    }
  }

  override fun remove(index: Int, count: Int) {
    children.removeRange(index, index + count)
  }

  override fun clear() {
    children.clear()
  }
}
