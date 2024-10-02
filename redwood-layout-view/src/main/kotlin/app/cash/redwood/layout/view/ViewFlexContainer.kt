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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener as OnScrollChangeListenerCompat
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Px
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.ViewGroupChildren
import app.cash.redwood.yoga.Direction
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.isHorizontal

internal class ViewFlexContainer(
  private val context: Context,
  private val direction: FlexDirection,
) : YogaFlexContainer<View>,
  ChangeListener {
  private val yogaLayout: YogaLayout = YogaLayout(
    context,
    applyModifier = { node, index ->
      node.applyModifier(children.widgets[index].modifier, density)
    },
  )
  override val rootNode: Node get() = yogaLayout.rootNode
  override val density = Density(context.resources)

  private val hostView = HostView()
  override val value: View get() = hostView

  override val children = ViewGroupChildren(
    yogaLayout,
    insert = { index, view ->
      yogaLayout.rootNode.children.add(index, view.asNode())
      yogaLayout.addView(view, index)
    },
    remove = { index, count ->
      yogaLayout.rootNode.children.remove(index, count)
      yogaLayout.removeViews(index, count)
    },
  )

  private var onScroll: ((Px) -> Unit)? = null

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
    hostView.updateLayoutParams {
      this.width = if (width == Constraint.Fill) MATCH_PARENT else WRAP_CONTENT
    }
  }

  override fun height(height: Constraint) {
    hostView.updateLayoutParams {
      this.height = if (height == Constraint.Fill) MATCH_PARENT else WRAP_CONTENT
    }
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

  private inner class HostView : FrameLayout(context) {
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
      layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
      updateViewHierarchy()
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

private fun View.asNode(): Node {
  return Node().apply {
    measureCallback = ViewMeasureCallback(this@asNode)
  }
}
