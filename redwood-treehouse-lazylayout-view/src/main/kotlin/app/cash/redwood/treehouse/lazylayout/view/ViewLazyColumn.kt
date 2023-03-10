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

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.isVisible
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
import app.cash.zipline.ZiplineScope

internal class ViewLazyColumn<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: TreehouseView.WidgetSystem<A>,
  override val value: RecyclerView,
) : LazyColumn<View> {
  override var layoutModifiers: LayoutModifier = LayoutModifier

  private val adapter = LazyListIntervalContentAdapter(
    treehouseApp,
    widgetSystem,
  )

  init {
    value.apply {
      layoutManager = LinearLayoutManager(value.context)
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
    value.adapter = adapter
    value.recycledViewPool.setMaxRecycledViews(adapter.getItemViewType(RecyclerView.NO_POSITION), 100)
  }

  override fun intervals(intervals: List<LazyListIntervalContent>) {
    adapter.submitList(intervals.flatMap { interval -> List(interval.count) { interval } })
  }

  private class LazyListIntervalContentAdapter<A : AppService>(
    private val treehouseApp: TreehouseApp<A>,
    private val widgetSystem: TreehouseView.WidgetSystem<A>,
  ) : ListAdapter<LazyListIntervalContent, LazyListIntervalContentAdapter.ViewHolder<A>>(LazyListIntervalContentDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<A> {
      val container = FrameLayout(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      }
      return ViewHolder(container, treehouseApp, widgetSystem)
    }

    override fun onBindViewHolder(holder: ViewHolder<A>, position: Int) {
      holder.itemTreehouseWidgetViewLoadState = LoadState.Loading
      holder.interval = getItem(position)!!
      holder.onLoadStateChange()
    }

    override fun onViewRecycled(holder: ViewHolder<A>) {
      holder.ziplineScope.close()
    }

    private class ViewHolder<A : AppService>(
      val container: FrameLayout,
      treehouseApp: TreehouseApp<A>,
      widgetSystem: TreehouseView.WidgetSystem<A>,
    ) : RecyclerView.ViewHolder(container) {

      val ziplineScope = ZiplineScope()

      var interval: LazyListIntervalContent? = null

      val blankView = FrameLayout(itemView.context).apply {
        layoutParams = FrameLayout.LayoutParams(
          MATCH_PARENT,
          TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1000F, itemView.resources.displayMetrics).toInt(),
        )
      }

      var placeholderTreehouseWidgetViewLoadState = LoadState.Loading
      val placeholderTreehouseWidgetViewCodeListener = object : TreehouseView.CodeListener() {
        override fun onCodeLoaded(initial: Boolean) {
          val newState = LoadState.Loaded
          if (placeholderTreehouseWidgetViewLoadState == newState) return
          placeholderTreehouseWidgetViewLoadState = newState
          onLoadStateChange()
        }
      }
      val placeholderTreehouseWidgetView: TreehouseWidgetView<A> = TreehouseWidgetView(container.context, widgetSystem, placeholderTreehouseWidgetViewCodeListener)
        .apply {
          treehouseApp.renderTo(this)

          layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL
          }
        }

      var itemTreehouseWidgetViewLoadState = LoadState.Loading
      val itemTreehouseWidgetViewCodeListener = object : TreehouseView.CodeListener() {
        override fun onCodeLoaded(initial: Boolean) {
          val newState = LoadState.Loaded
          if (itemTreehouseWidgetViewLoadState == newState) return
          itemTreehouseWidgetViewLoadState = newState
          onLoadStateChange()
        }
      }
      val itemTreehouseWidgetView: TreehouseWidgetView<A> = TreehouseWidgetView(container.context, widgetSystem, itemTreehouseWidgetViewCodeListener)
        .apply {
          treehouseApp.renderTo(this)

          layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER_HORIZONTAL
          }
        }

      fun onLoadStateChange() {
        val interval = checkNotNull(interval)

        val holder: ViewHolder<A> = this
        holder.container.removeAllViews()
        when {
          holder.itemTreehouseWidgetViewLoadState == LoadState.Loaded -> {
            // Show the item
            holder.itemTreehouseWidgetView.isVisible = true
            holder.container.addView(holder.itemTreehouseWidgetView)
          }
          holder.placeholderTreehouseWidgetViewLoadState == LoadState.Loading -> {
            // Show the blank
            holder.container.addView(holder.blankView)

            // Start loading the placeholder
            holder.placeholderTreehouseWidgetView.isVisible = false
            holder.container.addView(holder.placeholderTreehouseWidgetView)
            holder.placeholderTreehouseWidgetView.setContent {
              interval.placeholderProvider.get()
            }
          }
          holder.placeholderTreehouseWidgetViewLoadState == LoadState.Loaded -> {
            // Show the placeholder.
            holder.placeholderTreehouseWidgetView.isVisible = true
            holder.container.addView(holder.placeholderTreehouseWidgetView)

            // Start loading the item
            holder.itemTreehouseWidgetView.isVisible = false
            holder.container.addView(holder.itemTreehouseWidgetView)
            val position = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: return
            holder.itemTreehouseWidgetView.setContent {
              interval.itemProvider.get(position)
            }
          }
        }
      }
    }

    enum class LoadState {
      Loading,
      Loaded,
    }
  }

  private object LazyListIntervalContentDiffCallback : DiffUtil.ItemCallback<LazyListIntervalContent>() {
    override fun areItemsTheSame(
      oldItem: LazyListIntervalContent,
      newItem: LazyListIntervalContent,
    ) = oldItem === newItem

    override fun areContentsTheSame(
      oldItem: LazyListIntervalContent,
      newItem: LazyListIntervalContent,
    ) = oldItem == newItem
  }
}
