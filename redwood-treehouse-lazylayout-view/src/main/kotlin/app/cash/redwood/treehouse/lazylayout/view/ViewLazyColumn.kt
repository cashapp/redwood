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
package app.cash.redwood.treehouse.lazylayout.view

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
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseWidgetView
import app.cash.redwood.treehouse.lazylayout.api.LazyListIntervalContent
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget

private data class LazyContentItem(
  val index: Int,
  val item: LazyListIntervalContent.Item,
)

internal class ViewLazyColumn<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: TreehouseView.WidgetSystem<A>,
  override val value: RecyclerView,
) : LazyColumn<View> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val placeholder = MutableListChildren<View>()

  private val adapter = LazyContentItemListAdapter(
    treehouseApp,
    widgetSystem,
    placeholder,
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
        List(interval.count) { index ->
          LazyContentItem(index, interval.itemProvider)
        }
      },
    )
  }

  private class LazyContentItemListAdapter<A : AppService>(
    private val treehouseApp: TreehouseApp<A>,
    private val widgetSystem: TreehouseView.WidgetSystem<A>,
    private val placeholder: List<Widget<View>>,
  ) : ListAdapter<LazyContentItem, ViewHolder<A>>(LazyContentItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<A> {
      val container = FrameLayout(parent.context)
      // TODO Clone placeholder.
      placeholder.forEachIndexed { index, child -> container.addView(child.value, index) }
      return ViewHolder(container, treehouseApp, widgetSystem)
    }

    override fun onBindViewHolder(holder: ViewHolder<A>, position: Int) {
      val itemContent = currentList[position]
      holder.treehouseWidgetView.setContent {
        itemContent.item.get(itemContent.index)
      }
    }
  }

  private class ViewHolder<A : AppService>(
    container: FrameLayout,
    treehouseApp: TreehouseApp<A>,
    widgetSystem: TreehouseView.WidgetSystem<A>,
  ) : RecyclerView.ViewHolder(container) {
    val treehouseWidgetView = TreehouseWidgetView(container.context, widgetSystem)
      .apply {
        treehouseApp.renderTo(this)

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
