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
package app.cash.redwood.layout.view

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.LayoutDirection
import android.view.View
import android.view.View.OnScrollChangeListener
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener as OnScrollChangeListenerCompat
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.yoga.Direction
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.isHorizontal

internal class ViewColumn(context: Context) :
  Column<View>,
  ChangeListener {
  private val delegate = ViewFlexContainer(context, FlexDirection.Column)

  override val value get() = delegate.value
  override var modifier by delegate::modifier

  override val children get() = delegate.children

  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun overflow(overflow: Overflow) = delegate.overflow(overflow)
  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) = delegate.crossAxisAlignment(horizontalAlignment)
  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) = delegate.mainAxisAlignment(verticalAlignment)
  override fun onScroll(onScroll: ((Px) -> Unit)?) = delegate.onScroll(onScroll)
  override fun onEndChanges() = delegate.onEndChanges()
}

internal class ViewRow(context: Context) :
  Row<View>,
  ChangeListener {
  private val delegate = ViewFlexContainer(context, FlexDirection.Row)

  override val value get() = delegate.value
  override var modifier by delegate::modifier

  override val children get() = delegate.children

  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun overflow(overflow: Overflow) = delegate.overflow(overflow)
  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) = delegate.mainAxisAlignment(horizontalAlignment)
  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) = delegate.crossAxisAlignment(verticalAlignment)
  override fun onScroll(onScroll: ((Px) -> Unit)?) = delegate.onScroll(onScroll)
  override fun onEndChanges() = delegate.onEndChanges()
}

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : YogaFlexContainer<View>,
  ChangeListener {
  private val yogaLayout: YogaLayout = YogaLayout(context)
  override val rootNode: Node get() = yogaLayout.rootNode
  override val density = Density(context.resources)

  private val hostView = HostView()
  override val value: View get() = hostView

  override val children = ViewGroupChildren(
    yogaLayout,
    insert = { index, widget ->
      val view = widget.value

      val node = Node(view)
      yogaLayout.rootNode.children.add(index, node)

      // Always apply changes *after* adding a node to its parent.
      node.applyModifier(widget.modifier, density)

      yogaLayout.addView(view, index)
    },
    remove = { index, count ->
      yogaLayout.rootNode.children.remove(index, count)
      yogaLayout.removeViews(index, count)
    },
    onModifierUpdated = { index, widget ->
      val node = yogaLayout.rootNode.children[index]
      node.applyModifier(widget.modifier, density)
      yogaLayout.requestLayout()
    },
  )

  internal var onScroll: ((Px) -> Unit)? = null

  override var modifier: Modifier = Modifier

  init {
    yogaLayout.rootNode.direction = when (hostView.resources.configuration.layoutDirection) {
      LayoutDirection.LTR -> Direction.LTR
      LayoutDirection.RTL -> Direction.RTL
      else -> throw AssertionError()
    }
    yogaLayout.rootNode.flexDirection = direction
  }

  override fun width(width: Constraint) {
    yogaLayout.widthConstraint = width
  }

  override fun height(height: Constraint) {
    yogaLayout.heightConstraint = height
  }

  override fun overflow(overflow: Overflow) {
    hostView.scrollEnabled = when (overflow) {
      Overflow.Clip -> false
      Overflow.Scroll -> true
      else -> throw AssertionError()
    }
  }

  override fun onScroll(onScroll: ((Px) -> Unit)?) {
    this.onScroll = onScroll
    hostView.attachOrDetachScrollListeners()
  }

  override fun onEndChanges() {
    hostView.invalidate()
    hostView.requestLayout()
    yogaLayout.invalidate()
    yogaLayout.requestLayout()
  }

  private inner class HostView : ViewGroup(context) {
    var scrollEnabled = false
      set(new) {
        val old = field
        field = new
        if (old != new) {
          updateViewHierarchy()
        }
      }

    // Either OnScrollChangeListenerCompat or OnScrollChangeListener. Created lazily.
    private var onScrollListener: Any? = null

    init {
      updateViewHierarchy()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
      for (child in children) {
        child.layout(0, 0, right - left, bottom - top)
      }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
      var maxWidth = 0
      var maxHeight = 0
      for (child in children) {
        child.measure(widthMeasureSpec, heightMeasureSpec)
        maxWidth = maxOf(maxWidth, child.measuredWidth)
        maxHeight = maxOf(maxHeight, child.measuredHeight)
      }
      setMeasuredDimension(maxWidth, maxHeight)
    }

    fun attachOrDetachScrollListeners() {
      val child = getChildAt(0)
      if (child is NestedScrollView) {
        val listener = (onScrollListener as OnScrollChangeListenerCompat?)
          ?: OnScrollChangeListenerCompat { _, _, scrollY, _, _ -> onScroll?.invoke(Px(scrollY.toDouble())) }
            .also { onScrollListener = it }
        child.setOnScrollChangeListener(listener)
      } else if (SDK_INT >= 23 && child is HorizontalScrollView) {
        val listener = (onScrollListener as OnScrollChangeListener?)
          ?: OnScrollChangeListener { _, scrollX, _, _, _ -> onScroll?.invoke(Px(scrollX.toDouble())) }
            .also { onScrollListener = it }
        child.setOnScrollChangeListener(listener)
      }
    }

    private fun updateViewHierarchy() {
      removeAllViews()
      (yogaLayout.parent as ViewGroup?)?.removeView(yogaLayout)

      if (scrollEnabled) {
        addView(newScrollView().apply { addView(yogaLayout) })
        attachOrDetachScrollListeners()
      } else {
        addView(yogaLayout)
      }
    }

    private fun newScrollView(): ViewGroup {
      return if (direction.isHorizontal) {
        HorizontalScrollView(context).apply {
          isFillViewport = true
        }
      } else {
        NestedScrollView(context).apply {
          isFillViewport = true
        }
      }.apply {
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
      }
    }
  }
}

private fun Node(view: View): Node {
  val result = Node()
  result.measureCallback = ViewMeasureCallback
  result.context = view
  return result
}
