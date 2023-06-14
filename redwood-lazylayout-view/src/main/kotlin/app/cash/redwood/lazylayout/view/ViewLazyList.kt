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

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.core.view.doOnDetach
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

private const val VIEW_TYPE_PLACEHOLDER = 1
private const val VIEW_TYPE_ITEM = 2

internal class Placeholders(
  private val recycledViewPool: RecyclerView.RecycledViewPool,
) : Widget.Children<View> {
  private var poolSize = 0
  private val pool = ArrayDeque<Widget<View>>()

  fun takeOrNull(): Widget<View>? = pool.removeLastOrNull()

  override fun insert(index: Int, widget: Widget<View>) {
    poolSize++
    pool += widget
    recycledViewPool.setMaxRecycledViews(VIEW_TYPE_PLACEHOLDER, poolSize)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {}
  override fun remove(index: Int, count: Int) {}
  override fun onModifierUpdated() {}
}

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

  override fun onModifierUpdated() {
  }
}

internal open class ViewLazyList(context: Context) : LazyList<View> {
  private val scope = MainScope()

  internal val recyclerView: RecyclerView = RecyclerView(context)

  override var modifier: Modifier = Modifier

  final override val placeholder = Placeholders(recyclerView.recycledViewPool)

  private val linearLayoutManager = LinearLayoutManager(recyclerView.context)
  private val adapter = LazyContentItemListAdapter(placeholder)
  private var onViewportChanged: ((firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit)? = null
  private var viewport = IntRange.EMPTY

  override val value: View get() = recyclerView

  final override val items = Items(adapter)

  init {
    adapter.items = items
    recyclerView.apply {
      layoutManager = linearLayoutManager
      layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

      // TODO Dynamically set the max recycled views for VIEW_TYPE_ITEM
      recycledViewPool.setMaxRecycledViews(VIEW_TYPE_ITEM, 30)
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

  override fun width(width: Constraint) {
    recyclerView.updateLayoutParams {
      this.width = if (width == Constraint.Fill) MATCH_PARENT else WRAP_CONTENT
    }
  }

  override fun height(height: Constraint) {
    recyclerView.updateLayoutParams {
      this.height = if (height == Constraint.Fill) MATCH_PARENT else WRAP_CONTENT
    }
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

  @SuppressLint("NotifyDataSetChanged")
  override fun itemsBefore(itemsBefore: Int) {
    items.itemsBefore = itemsBefore

    // TODO Replace notifyDataSetChanged with atomic change events
    //  notifyItemRangeInserted causes an onScrolled event to be emitted.
    //  This incorrectly updates the viewport, which then shifts the loaded items window.
    //  This then increases the value of itemsBefore,
    //  and the cycle continues until the backing dataset is exhausted.
    adapter.notifyDataSetChanged()
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

  private class LazyContentItemListAdapter(
    val placeholders: Placeholders,
  ) : RecyclerView.Adapter<ViewHolder>() {
    lateinit var items: Items<ViewHolder>

    /**
     * When we haven't loaded enough placeholders for the viewport height, we set a blank view while
     * we load request more placeholders. This "meta" placeholder needs a non-zero height, so we
     * don't load an infinite number of zero height meta placeholders.
     *
     * We set this height to the last available item height, or a hardcoded value in the case when
     * no views have been laid out, but a meta placeholder has been requested.
     */
    private var lastItemHeight = 100
      set(value) {
        if (value > 0) {
          field = value
        }
      }

    override fun getItemCount(): Int = items.itemsBefore + items.widgets.size + items.itemsAfter

    override fun getItemViewType(position: Int): Int {
      val index = position - items.itemsBefore
      return if (index in items.widgets.indices) VIEW_TYPE_ITEM else VIEW_TYPE_PLACEHOLDER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val container = Container(parent.context)
      return when (viewType) {
        VIEW_TYPE_PLACEHOLDER -> ViewHolder.Placeholder(container)
        VIEW_TYPE_ITEM -> ViewHolder.Item(container)
        else -> error("Unrecognized viewType: $viewType")
      }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      lastItemHeight = holder.itemView.height
      when (holder) {
        is ViewHolder.Placeholder -> {
          if (holder.container.childCount == 0) {
            val placeholder = placeholders.takeOrNull()
            if (placeholder != null) {
              holder.container.addView(placeholder.value)
              holder.itemView.updateLayoutParams { height = WRAP_CONTENT }
            } else if (holder.container.height == 0) {
              // This occurs when the ViewHolder has been freshly created, so we set the container
              // to a non-zero height so that it's visible.
              holder.itemView.updateLayoutParams { height = lastItemHeight }
            }
          }
        }
        is ViewHolder.Item -> {
          val index = position - items.itemsBefore
          val view = items.widgets[index].value
          holder.container.removeAllViews()
          (view.parent as? FrameLayout)?.removeAllViews()
          holder.container.addView(view)
        }
      }
    }
  }

  class Container(context: Context) : FrameLayout(context) {
    init {
      layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    // Identical to the implementation of [YogaLayout.generateDefaultLayoutParams].
    override fun generateDefaultLayoutParams(): LayoutParams {
      return LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }
  }

  sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    class Placeholder(val container: Container) : ViewHolder(container)
    class Item(val container: Container) : ViewHolder(container)
  }
}

internal class ViewRefreshableLazyList(
  context: Context,
) : ViewLazyList(context), RefreshableLazyList<View> {

  private val swipeRefreshLayout = SwipeRefreshLayout(context)

  override val value: View get() = swipeRefreshLayout

  init {
    swipeRefreshLayout.apply {
      addView(recyclerView)
      // TODO Dynamically update width and height of RefreshableViewLazyList when set
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
