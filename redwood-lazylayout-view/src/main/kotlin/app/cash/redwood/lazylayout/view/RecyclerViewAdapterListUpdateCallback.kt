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
package app.cash.redwood.lazylayout.view

import androidx.recyclerview.widget.RecyclerView
import app.cash.redwood.lazylayout.widget.ListUpdateCallback

internal class RecyclerViewAdapterListUpdateCallback(
  private val adapter: RecyclerView.Adapter<*>,
) : ListUpdateCallback {

  override fun onInserted(position: Int, count: Int) {
    adapter.notifyItemRangeInserted(position, count)
  }

  override fun onMoved(fromPosition: Int, toPosition: Int, count: Int) {
    check(count == 1)
    // TODO Support arbitrary count.
    adapter.notifyItemMoved(fromPosition, toPosition)
  }

  override fun onRemoved(position: Int, count: Int) {
    adapter.notifyItemRangeRemoved(position, count)
  }
}
