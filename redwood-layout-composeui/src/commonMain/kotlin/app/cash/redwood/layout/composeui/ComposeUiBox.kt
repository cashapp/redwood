/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.layout.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastMap
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.modifier.Height
import app.cash.redwood.layout.modifier.HorizontalAlignment
import app.cash.redwood.layout.modifier.Margin as MarginModifier
import app.cash.redwood.layout.modifier.Size
import app.cash.redwood.layout.modifier.VerticalAlignment
import app.cash.redwood.layout.modifier.Width
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.compose.ComposeWidgetChildren

internal class ComposeUiBox(
  private val backgroundColor: Int = 0,
) : Box<@Composable () -> Unit> {
  private var modifierTick by mutableIntStateOf(0)
  override val children = ComposeWidgetChildren(onModifierUpdated = { modifierTick++ })

  private var width by mutableStateOf(Constraint.Wrap)
  private var height by mutableStateOf(Constraint.Wrap)
  private var margin by mutableStateOf(Margin.Zero)
  private var alignment by mutableStateOf(BiasAlignment(-1f, -1f))
  private var matchParentWidth by mutableStateOf(false)
  private var matchParentHeight by mutableStateOf(false)
  private var density by mutableStateOf(Density(1.0))

  override val value = @Composable {
    density = Density(LocalDensity.current.density.toDouble())

    Box(
      childrenLayoutInfo = computeChildrenLayoutInfo(),
      modifier = computeModifier(),
    ) {
      children.Render()
    }
  }

  @Composable
  private fun computeChildrenLayoutInfo(): BoxChildrenLayoutInfo {
    // Observe the layout modifier count so we recompose if it changes.
    modifierTick

    return BoxChildrenLayoutInfo(
      infos = children.widgets.fastMap { it.modifier.toBoxChildLayoutInfo() },
    )
  }

  private fun RedwoodModifier.toBoxChildLayoutInfo(): BoxChildLayoutInfo {
    var alignment = alignment
    var matchParentWidth = matchParentWidth
    var matchParentHeight = matchParentHeight
    var requestedHeight: Dp? = null

    forEachScoped { childModifier ->
      when (childModifier) {
        is HorizontalAlignment -> {
          matchParentWidth = childModifier.alignment == CrossAxisAlignment.Stretch
          alignment = BiasAlignment(
            horizontalBias = alignment.horizontalBias,
            verticalBias = childModifier.alignment.toBias(),
          )
        }

        is VerticalAlignment -> {
          matchParentHeight = childModifier.alignment == CrossAxisAlignment.Stretch
          alignment = BiasAlignment(
            horizontalBias = childModifier.alignment.toBias(),
            verticalBias = alignment.verticalBias,
          )
        }

        is Width -> {
          // TODO
        }

        is Height -> {
          requestedHeight = childModifier.height.toDp()
        }

        is Size -> {
          // TODO
        }

        is MarginModifier -> {
          // TODO
        }
      }
    }

    // TODO: Support these cases.
    if (width == Constraint.Wrap && matchParentWidth) {
      matchParentWidth = false
    }
    if (height == Constraint.Wrap && matchParentHeight) {
      matchParentHeight = false
    }

    return BoxChildLayoutInfo(
      alignment = alignment,
      matchParentWidth = matchParentWidth,
      matchParentHeight = matchParentHeight,
      requestedHeight = requestedHeight,
    )
  }

  @Composable
  private fun computeModifier(): Modifier {
    var modifier: Modifier = Modifier
    modifier = if (width == Constraint.Fill) {
      modifier.fillMaxWidth()
    } else {
      modifier.wrapContentWidth(Alignment.Start, unbounded = true)
    }
    modifier = if (height == Constraint.Fill) {
      modifier.fillMaxHeight()
    } else {
      modifier.wrapContentHeight(Alignment.Top, unbounded = true)
    }
    if (backgroundColor != 0) {
      modifier = modifier.background(Color(backgroundColor))
    }
    modifier = margin.let { margin ->
      modifier.padding(
        start = margin.start.toDp(),
        top = margin.top.toDp(),
        end = margin.end.toDp(),
        bottom = margin.bottom.toDp(),
      )
    }
    return modifier
  }

  override var modifier: RedwoodModifier = RedwoodModifier

  override fun width(width: Constraint) {
    this.width = width
  }

  override fun height(height: Constraint) {
    this.height = height
  }

  override fun margin(margin: Margin) {
    this.margin = margin
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    matchParentWidth = horizontalAlignment == CrossAxisAlignment.Stretch
    alignment = BiasAlignment(
      horizontalBias = horizontalAlignment.toBias(),
      verticalBias = alignment.verticalBias,
    )
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    matchParentHeight = verticalAlignment == CrossAxisAlignment.Stretch
    alignment = BiasAlignment(
      horizontalBias = alignment.horizontalBias,
      verticalBias = verticalAlignment.toBias(),
    )
  }

  private fun CrossAxisAlignment.toBias() = when (this) {
    CrossAxisAlignment.Stretch,
    CrossAxisAlignment.Start,
    -> -1f
    CrossAxisAlignment.Center -> 0f
    CrossAxisAlignment.End -> 1f
    else -> throw AssertionError()
  }
}
