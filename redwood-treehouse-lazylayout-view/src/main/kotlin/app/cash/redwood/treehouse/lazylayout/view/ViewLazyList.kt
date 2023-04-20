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
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingDataAdapter
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.Content
import app.cash.redwood.treehouse.HostConfiguration
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.TreehouseWidgetView
import app.cash.redwood.treehouse.lazylayout.api.LazyListInterval
import app.cash.redwood.treehouse.lazylayout.widget.LazyList
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class ViewLazyList<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  widgetSystem: WidgetSystem,
  override val value: RecyclerView,
) : LazyList<View> {
  private val scope = MainScope()

  override var layoutModifiers: LayoutModifier = LayoutModifier

  private val linearLayoutManager = LinearLayoutManager(value.context)
  private val adapter = LazyContentItemListAdapter(widgetSystem)

  init {
    value.apply {
      layoutManager = linearLayoutManager
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
    value.adapter = adapter
    value.addOnAttachStateChangeListener(
      object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) {}

        override fun onViewDetachedFromWindow(view: View) {
          view.removeOnAttachStateChangeListener(this)
          scope.cancel()
        }
      },
    )
  }

  override fun isVertical(isVertical: Boolean) {
    linearLayoutManager.orientation = if (isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
  }

  override fun intervals(intervals: List<LazyListInterval>) {
    // TODO Don't hardcode pageSizes
    // TODO Enable placeholder support
    // TODO Set a maxSize so we don't keep _too_ many views in memory
    val pager = Pager(PagingConfig(pageSize = 30, initialLoadSize = 30, enablePlaceholders = false)) {
      ItemPagingSource(treehouseApp, intervals)
    }
    scope.launch {
      pager.flow.collectLatest { pagingData ->
        adapter.submitData(pagingData)
      }
    }
  }

  private class ItemPagingSource<A : AppService>(
    private val treehouseApp: TreehouseApp<A>,
    private val intervals: List<LazyListInterval>,
  ) : PagingSource<Int, Content>() {

    override suspend fun load(
      params: LoadParams<Int>,
    ): LoadResult<Int, Content> {
      val key = params.key ?: 0
      val count = intervals.sumOf(LazyListInterval::count)
      val limit = when (params) {
        is LoadParams.Prepend<*> -> minOf(key, params.loadSize)
        else -> params.loadSize
      }.coerceAtMost(count)
      val offset = when (params) {
        is LoadParams.Prepend<*> -> maxOf(0, key - params.loadSize)
        is LoadParams.Append<*> -> key
        is LoadParams.Refresh<*> -> if (key >= count) maxOf(0, count - params.loadSize) else key
      }
      val nextPosToLoad = offset + limit
      val loadResult = LoadResult.Page(
        data = List(if (nextPosToLoad <= count) limit else count - offset) { index ->
          val itemContentSource = TreehouseContentSource<A> {
            var interval = IndexedValue(index + offset, intervals.first())
            for (nextInterval in intervals.drop(1)) {
              if (interval.index < interval.value.count) break
              interval = IndexedValue(interval.index - interval.value.count, nextInterval)
            }
            interval.value.itemProvider.get(interval.index)
          }
          treehouseApp.createContent(itemContentSource).apply {
            // TODO Create a public API that accepts an Android Context and returns the corresponding HostConfiguration
            preload(HostConfiguration(darkMode = true))
            awaitContent()
          }
        },
        prevKey = offset.takeIf { it > 0 && limit > 0 },
        nextKey = nextPosToLoad.takeIf { limit > 0 && it < count },
        itemsBefore = offset,
        itemsAfter = maxOf(0, count - nextPosToLoad),
      )
      return if (invalid) LoadResult.Invalid() else loadResult
    }

    override fun getRefreshKey(state: PagingState<Int, Content>) = state.anchorPosition
  }

  private class LazyContentItemListAdapter(
    private val widgetSystem: WidgetSystem,
  ) : PagingDataAdapter<Content, ViewHolder>(ContentDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
      TreehouseWidgetView(parent.context, widgetSystem).apply {
        layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
          gravity = Gravity.CENTER_HORIZONTAL
        }
      },
    )

    override fun onViewRecycled(holder: ViewHolder) {
      if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION) {
        getItem(holder.bindingAdapterPosition)!!.unbind()
      }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      getItem(position)!!.bind(holder.treehouseWidgetView)
    }
  }

  private class ViewHolder(val treehouseWidgetView: TreehouseWidgetView) : RecyclerView.ViewHolder(treehouseWidgetView)

  private object ContentDiffCallback : DiffUtil.ItemCallback<Content>() {
    override fun areItemsTheSame(
      oldItem: Content,
      newItem: Content,
    ) = oldItem === newItem

    override fun areContentsTheSame(
      oldItem: Content,
      newItem: Content,
    ) = oldItem == newItem
  }
}
