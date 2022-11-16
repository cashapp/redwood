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
package app.cash.zipline.samples.emojisearch.views

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseWidgetView
import example.schema.widget.LazyColumn
import example.values.LazyListIntervalContent

private data class LazyContentItem(
  val index: Int,
  val item: LazyListIntervalContent.Item,
)

class ViewLazyColumn<T : Any>(
  treehouseApp: TreehouseApp<T>,
  override val value: RecyclerView,
) : LazyColumn<View> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  private val adapter = LazyContentItemListAdapter(
    treehouseApp,
    contentHeight = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP,
      64F,
      value.resources.displayMetrics,
    ).toInt(),
  )

  init {
    value.apply {
      layoutManager = LinearLayoutManager(value.context)
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
    value.adapter = adapter
  }

  override fun intervals(intervals: List<LazyListIntervalContent>) {
    adapter.submitList(
      intervals.flatMap { interval ->
        (0 until interval.count).map { index ->
          LazyContentItem(index, interval.itemProvider)
        }
      },
    )
  }

  private class LazyContentItemListAdapter<T : Any>(
    private val treehouseApp: TreehouseApp<T>,
    private val contentHeight: Int,
  ) : ListAdapter<LazyContentItem, ViewHolder<T>>(LazyContentItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
      val container = FrameLayout(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, contentHeight)
      }
      return ViewHolder(container, treehouseApp)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
      val itemContent = currentList[position]
      holder.treehouseWidgetView.setContent {
        itemContent.item.get(itemContent.index)
      }
    }
  }

  private class ViewHolder<T : Any>(
    container: FrameLayout,
    treehouseApp: TreehouseApp<T>
  ) : RecyclerView.ViewHolder(container) {
    val treehouseWidgetView = TreehouseWidgetView(container.context, treehouseApp).apply {
      layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        gravity = Gravity.CENTER_HORIZONTAL
      }

      container.addView(this)
    }
  }

  private object LazyContentItemDiffCallback : DiffUtil.ItemCallback<LazyContentItem>() {
    override fun areItemsTheSame(
      oldItem: LazyContentItem,
      newItem: LazyContentItem,
    ) = oldItem === newItem

    override fun areContentsTheSame(
      oldItem: LazyContentItem,
      newItem: LazyContentItem,
    ) = oldItem == newItem

  }
}
