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
@file:Suppress("FunctionName")

package app.cash.redwood.lazylayout.view

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.doOnDetach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.cash.redwood.LayoutModifier
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

internal class Items<VH : RecyclerView.ViewHolder>(
  private val adapter: RecyclerView.Adapter<VH>,
) : Widget.Children<View> {
  internal var itemsBefore = 0
  internal var itemsAfter = 0

  private val _widgets = MutableListChildren<View>()
  val widgets: List<Widget<View>> get() = _widgets

  override fun insert(index: Int, widget: Widget<View>) {
    _widgets.insert(index, widget)
    adapter.notifyItemInserted(itemsBefore + index)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)
    check(count == 1)
    // TODO Support arbitrary count.
    adapter.notifyItemMoved(itemsBefore + fromIndex, itemsBefore + toIndex)
  }

  override fun remove(index: Int, count: Int) {
    _widgets.remove(index, count)
    adapter.notifyItemRangeRemoved(itemsBefore + index, count)
  }

  override fun onLayoutModifierUpdated() {
  }
}

/**
 * Public function to allow downstream factories to create their own LazyList
 */
public fun ViewLazyList(
  recyclerViewFactory: () -> RecyclerView,
): LazyList<View> = ViewLazyListImpl(recyclerViewFactory)

/**
 * Public function to allow downstream factories to create their own RefreshableLazyList
 */
public fun ViewRefreshableLazyList(
  recyclerViewFactory: () -> RecyclerView,
  swipeRefreshLayoutFactory: () -> SwipeRefreshLayout,
): RefreshableLazyList<View> = RefreshableViewLazyListImpl(recyclerViewFactory, swipeRefreshLayoutFactory)

internal open class ViewLazyListImpl(
  recyclerViewFactory: () -> RecyclerView,
) : LazyList<View> {
  private val scope = MainScope()

  internal val recyclerView: RecyclerView by lazy { recyclerViewFactory() }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  private val linearLayoutManager = LinearLayoutManager(recyclerView.context)
  private val adapter = LazyContentItemListAdapter()
  private var onViewportChanged: ((firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit)? = null
  private var viewport = IntRange.EMPTY

  override val value: View get() = recyclerView

  final override val items = Items(adapter)

  init {
    adapter.items = items
    recyclerView.apply {
      setHasFixedSize(true)
      layoutManager = linearLayoutManager

      // TODO: sizing should be controlled by LayoutModifiers
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

      addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            updateViewport()
          }
        },
      )

      doOnDetach {
        scope.cancel()
      }
    }
    recyclerView.adapter = adapter
  }

  override fun isVertical(isVertical: Boolean) {
    linearLayoutManager.orientation = if (isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
  }

  override fun onViewportChanged(onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit) {
    this.onViewportChanged = onViewportChanged
  }

  private fun updateViewport() {
    val newViewport = linearLayoutManager.findFirstVisibleItemPosition()..linearLayoutManager.findLastVisibleItemPosition()
    if (newViewport != viewport) {
      this.viewport = newViewport
      onViewportChanged?.invoke(newViewport.first, newViewport.last)
    }
  }

  override fun itemsBefore(itemsBefore: Int) {
    val delta = itemsBefore - items.itemsBefore
    items.itemsBefore = itemsBefore

    if (delta > 0) {
      adapter.notifyItemRangeInserted(itemsBefore - delta, delta)
    } else {
      adapter.notifyItemRangeRemoved(itemsBefore, -delta)
    }
  }

  override fun itemsAfter(itemsAfter: Int) {
    val delta = itemsAfter - items.itemsAfter
    items.itemsAfter = itemsAfter

    val positionStart = items.itemsBefore + items.widgets.size
    if (delta > 0) {
      adapter.notifyItemRangeInserted(positionStart, delta)
    } else {
      adapter.notifyItemRangeRemoved(positionStart, -delta)
    }
  }

  private class LazyContentItemListAdapter : RecyclerView.Adapter<ViewHolder>() {
    lateinit var items: Items<ViewHolder>

    override fun getItemCount(): Int = items.itemsBefore + items.widgets.size + items.itemsAfter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
      FrameLayout(parent.context).apply {
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      },
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val index = position - items.itemsBefore
      val view = if (index !in items.widgets.indices) {
        TextView(holder.itemView.context).apply {
          text = "Placeholder"
        }
      } else {
        items.widgets[index].value
      }
      holder.container.removeAllViews()
      (view.parent as? FrameLayout)?.removeAllViews()
      holder.container.addView(view)
    }
  }

  class ViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)
}

internal class RefreshableViewLazyListImpl(
  recyclerViewFactory: () -> RecyclerView,
  swipeRefreshLayoutFactory: () -> SwipeRefreshLayout,
) : ViewLazyListImpl(recyclerViewFactory), RefreshableLazyList<View> {

  private val swipeRefreshLayout by lazy { swipeRefreshLayoutFactory() }

  override val value: View get() = swipeRefreshLayout

  init {
    swipeRefreshLayout.apply {
      addView(recyclerView)
      layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }
  }

  override fun refreshing(refreshing: Boolean) {
    swipeRefreshLayout.isRefreshing = refreshing
  }

  override fun onRefresh(onRefresh: (() -> Unit)?) {
    swipeRefreshLayout.isEnabled = onRefresh != null
    swipeRefreshLayout.setOnRefreshListener(onRefresh)
  }
}
