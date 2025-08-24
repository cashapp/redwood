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
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.LazyListScrollProcessor
import app.cash.redwood.lazylayout.widget.LazyListUpdateProcessor
import app.cash.redwood.lazylayout.widget.LazyListUpdateProcessor.Binding
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget

private const val VIEW_TYPE_ITEM = 1

internal class ViewLazyList private constructor(
  val recyclerView: RecyclerView,
) : LazyList<View>,
  ChangeListener {
  private val adapter = LazyContentItemListAdapter()

  override var modifier: Modifier = Modifier

  private var crossAxisAlignment = CrossAxisAlignment.Start

  private val density = Density(recyclerView.context.resources)
  private val linearLayoutManager = object : LinearLayoutManager(recyclerView.context) {
    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams? = when (orientation) {
      RecyclerView.HORIZONTAL -> RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
      RecyclerView.VERTICAL -> RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      else -> null
    }
  }

  override val value: View get() = recyclerView

  private val processor = object : LazyListUpdateProcessor<ViewHolder, View>() {
    override fun createPlaceholder(original: View) =
      SizeOnlyPlaceholder(original, value.context)

    override fun insertRows(index: Int, count: Int) {
      adapter.notifyItemRangeInserted(index, count)
    }

    override fun deleteRows(index: Int, count: Int) {
      adapter.notifyItemRangeRemoved(index, count)
    }

    override fun setContent(view: ViewHolder, widget: Widget<View>?) {
      view.content = widget?.value
    }
  }

  private var isDoingProgrammaticScroll = false

  private val scrollProcessor = object : LazyListScrollProcessor() {
    override fun contentSize(): Int = processor.size

    override fun programmaticScroll(firstIndex: Int, animated: Boolean) {
      isDoingProgrammaticScroll = animated
      if (animated) {
        val smoothScroller: RecyclerView.SmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
          override fun getVerticalSnapPreference(): Int = SNAP_TO_START
        }
        smoothScroller.targetPosition = firstIndex
        linearLayoutManager.startSmoothScroll(smoothScroller)
      } else {
        linearLayoutManager.scrollToPositionWithOffset(firstIndex, 0)
      }
    }
  }

  override val items: Widget.Children<View> = processor.items

  override val placeholder: Widget.Children<View> = processor.placeholder

  constructor(context: Context) : this(RecyclerView(context))

  init {
    recyclerView.apply {
      layoutManager = linearLayoutManager
      layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

      // TODO Dynamically set the max recycled views for VIEW_TYPE_ITEM
      recycledViewPool.setMaxRecycledViews(VIEW_TYPE_ITEM, 30)
      addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (isDoingProgrammaticScroll) return // Only notify of user scrolls.

            val firstIndex = linearLayoutManager.findFirstVisibleItemPosition()
            if (firstIndex == RecyclerView.NO_POSITION) return
            val lastIndex = linearLayoutManager.findLastVisibleItemPosition()
            if (lastIndex == RecyclerView.NO_POSITION) return

            scrollProcessor.onUserScroll(firstIndex, lastIndex)
          }

          override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              isDoingProgrammaticScroll = false
            }
          }
        },
      )
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

  override fun margin(margin: Margin) {
    with(density) {
      recyclerView.updatePaddingRelative(
        start = margin.start.toPxInt(),
        top = margin.top.toPxInt(),
        end = margin.end.toPxInt(),
        bottom = margin.bottom.toPxInt(),
      )
    }
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    this.crossAxisAlignment = crossAxisAlignment

    // Layout params are invalid when crossAxisAlignment changes.
    for (binding in processor.bindings) {
      val view = binding.view ?: continue
      view.content?.layoutParams = createLayoutParams()
    }
  }

  override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) {
    scrollProcessor.onViewportChanged(onViewportChanged)
  }

  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) {
    scrollProcessor.scrollItemIndex(scrollItemIndex)
  }

  override fun isVertical(isVertical: Boolean) {
    linearLayoutManager.orientation = if (isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
  }

  override fun itemsBefore(itemsBefore: Int) {
    processor.itemsBefore(itemsBefore)
  }

  override fun itemsAfter(itemsAfter: Int) {
    processor.itemsAfter(itemsAfter)
  }

  override fun onEndChanges() {
    processor.onEndChanges()
    scrollProcessor.onEndChanges()
  }

  private fun createLayoutParams(): FrameLayout.LayoutParams {
    val layoutParams = if (crossAxisAlignment == CrossAxisAlignment.Stretch) {
      FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    } else {
      FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }
    layoutParams.apply {
      gravity = when (crossAxisAlignment) {
        CrossAxisAlignment.Start -> Gravity.START
        CrossAxisAlignment.Center -> Gravity.CENTER
        CrossAxisAlignment.End -> Gravity.END
        CrossAxisAlignment.Stretch -> Gravity.START
        else -> throw AssertionError()
      }
    }

    return layoutParams
  }

  private inner class LazyContentItemListAdapter : RecyclerView.Adapter<ViewHolder>() {
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

    override fun getItemCount(): Int = processor.size

    override fun getItemViewType(position: Int): Int = VIEW_TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val container = FrameLayout(parent.context)
      // [onBindViewHolder] is invoked before the default layout params are set, so
      // [View.getLayoutParams] will be null unless explicitly set.
      container.layoutParams = (parent as RecyclerView).layoutManager!!.generateDefaultLayoutParams()
      return ViewHolder(container)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      lastItemHeight = holder.itemView.height
      val binding = processor.bind(position, holder)
      holder.binding = binding
    }

    override fun onViewRecycled(holder: ViewHolder) {
      holder.binding?.unbind()
      holder.binding = null
    }
  }

  inner class ViewHolder(
    private val container: FrameLayout,
  ) : RecyclerView.ViewHolder(container) {
    var binding: Binding<ViewHolder, View>? = null

    var content: View? = null
      set(value) {
        field = value
        container.removeAllViews()

        if (value != null) {
          require(value.parent == null) {
            "Received $value with unexpected parent ${value.parent}"
          }
          value.layoutParams = createLayoutParams()
          container.addView(value)
        }
      }
  }
}

internal class ViewRefreshableLazyList(
  context: Context,
) : RefreshableLazyList<View>,
  ChangeListener {
  private val delegate = ViewLazyList(context)

  private val swipeRefreshLayout = SwipeRefreshLayout(context)

  override val value: View get() = swipeRefreshLayout
  override var modifier by delegate::modifier

  override val placeholder get() = delegate.placeholder
  override val items get() = delegate.items

  init {
    swipeRefreshLayout.apply {
      addView(delegate.recyclerView)
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

  override fun pullRefreshContentColor(@ColorInt pullRefreshContentColor: UInt) {
    swipeRefreshLayout.setColorSchemeColors(pullRefreshContentColor.toInt())
  }

  override fun isVertical(isVertical: Boolean) = delegate.isVertical(isVertical)
  override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) = delegate.onViewportChanged(onViewportChanged)
  override fun itemsBefore(itemsBefore: Int) = delegate.itemsBefore(itemsBefore)
  override fun itemsAfter(itemsAfter: Int) = delegate.itemsAfter(itemsAfter)
  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) = delegate.crossAxisAlignment(crossAxisAlignment)
  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) = delegate.scrollItemIndex(scrollItemIndex)
  override fun onEndChanges() = delegate.onEndChanges()
}

@SuppressLint("ViewConstructor")
private class SizeOnlyPlaceholder(
  private val original: View,
  context: Context,
) : View(context) {
  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    setMeasuredDimension(original.width, original.height)
  }
}
