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

import android.content.Context
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
import app.cash.redwood.treehouse.CodeListener
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.TreehouseWidgetView
import app.cash.redwood.treehouse.bindWhenReady
import app.cash.redwood.treehouse.lazylayout.api.LazyListIntervalContent
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class ViewLazyColumn<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val widgetSystem: WidgetSystem,
  override val value: RecyclerView,
) : LazyColumn<View> {
  private val scope = MainScope()

  override var layoutModifiers: LayoutModifier = LayoutModifier

  private val adapter = LazyContentItemListAdapter()

  init {
    value.apply {
      layoutManager = LinearLayoutManager(value.context)
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }
    value.adapter = adapter
  }

  override fun intervals(intervals: List<LazyListIntervalContent>) {
    // TODO Don't hardcode pageSizes
    // TODO Enable placeholder support
    // TODO Set a maxSize so we don't keep _too_ many views in memory
    val pager = Pager(PagingConfig(pageSize = 30, initialLoadSize = 30, enablePlaceholders = false), initialKey = 0) {
      ItemPagingSource(treehouseApp, widgetSystem, value.context, intervals.single())
    }
    scope.launch {
      pager.flow.collectLatest { pagingData ->
        adapter.submitData(pagingData)
      }
    }
  }

  private class ItemPagingSource<A : AppService>(
    private val treehouseApp: TreehouseApp<A>,
    private val widgetSystem: WidgetSystem,
    private val context: Context,
    private val interval: LazyListIntervalContent,
  ) : PagingSource<Int, TreehouseWidgetView>() {

    override suspend fun load(
      params: LoadParams<Int>,
    ): LoadResult<Int, TreehouseWidgetView> {
      val key = params.key ?: 0
      val count = interval.count
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
        data = List(limit) { index ->
          suspendCoroutine<TreehouseWidgetView> { continuation ->
            // TODO Put TreehouseWidgetView in the ViewHolder
            TreehouseWidgetView(context, widgetSystem, alwaysReadyForContent = true).apply {
              layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
              }
              val itemContentSource = TreehouseContentSource<A> {
                interval.itemProvider.get(index + offset)
              }
              itemContentSource.bindWhenReady(
                view = this,
                app = treehouseApp,
                codeListener = object : CodeListener() {
                  override fun onCodeLoaded(view: TreehouseView, initial: Boolean) {
                    continuation.resume(this@apply)
                  }
                },
              )
            }
          }
        },
        prevKey = offset.takeIf { it > 0 && limit > 0 },
        nextKey = nextPosToLoad.takeIf { limit > 0 && it < count },
        itemsBefore = offset,
        itemsAfter = maxOf(0, count - nextPosToLoad),
      )
      return if (invalid) LoadResult.Invalid() else loadResult
    }

    override fun getRefreshKey(state: PagingState<Int, TreehouseWidgetView>) =
      state.anchorPosition?.let { maxOf(0, it - (state.config.initialLoadSize / 2)) }
  }

  private class LazyContentItemListAdapter : PagingDataAdapter<TreehouseWidgetView, ViewHolder>(TreehouseWidgetViewDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val container = FrameLayout(parent.context).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      }
      return ViewHolder(container)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val treehouseWidgetView = getItem(position)!!
      (treehouseWidgetView.parent as? ViewGroup)?.removeView(treehouseWidgetView)
      holder.container.removeAllViews()
      holder.container.addView(treehouseWidgetView)
    }
  }

  private class ViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)

  private object TreehouseWidgetViewDiffCallback : DiffUtil.ItemCallback<TreehouseWidgetView>() {
    override fun areItemsTheSame(
      oldItem: TreehouseWidgetView,
      newItem: TreehouseWidgetView,
    ) = oldItem === newItem

    override fun areContentsTheSame(
      oldItem: TreehouseWidgetView,
      newItem: TreehouseWidgetView,
    ) = oldItem == newItem
  }
}
