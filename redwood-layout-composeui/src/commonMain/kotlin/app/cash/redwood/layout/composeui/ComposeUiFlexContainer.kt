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
package app.cash.redwood.layout.composeui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.compose.ComposeWidgetChildren
import app.cash.redwood.yoga.Direction
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import app.cash.redwood.yoga.isHorizontal

internal class ComposeUiFlexContainer(
  private val flexDirection: FlexDirection,
) : YogaFlexContainer<@Composable () -> Unit> {
  override val rootNode = Node().apply {
    flexDirection = this@ComposeUiFlexContainer.flexDirection
  }
  override val children = ComposeWidgetChildren()
  override var modifier: RedwoodModifier = RedwoodModifier

  private var recomposeTick by mutableIntStateOf(0)
  private var width by mutableStateOf(Constraint.Wrap)
  private var height by mutableStateOf(Constraint.Wrap)
  private var overflow by mutableStateOf(Overflow.Clip)
  private var margin by mutableStateOf(Margin.Zero)
  override var density = Density(1.0)

  internal var testOnlyModifier: Modifier? = null

  override fun width(width: Constraint) {
    this.width = width
  }

  override fun height(height: Constraint) {
    this.height = height
  }

  override fun margin(margin: Margin) {
    this.margin = margin
  }

  override fun overflow(overflow: Overflow) {
    this.overflow = overflow
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    super.crossAxisAlignment(crossAxisAlignment)
    invalidate()
  }

  override fun mainAxisAlignment(mainAxisAlignment: MainAxisAlignment) {
    super.mainAxisAlignment(mainAxisAlignment)
    invalidate()
  }

  private fun invalidate() {
    recomposeTick++
  }

  override val value = @Composable {
    Layout(
      content = {
        // Observe this so we can manually trigger recomposition.
        recomposeTick

        // Apply the margin.
        density = Density(LocalDensity.current.density.toDouble())
        with(rootNode) {
          direction = when (LocalLayoutDirection.current) {
            LayoutDirection.Ltr -> Direction.LTR
            LayoutDirection.Rtl -> Direction.RTL
          }
        }
        super.margin(margin)

        children.Render()
      },
      modifier = computeModifier(),
      measurePolicy = ::measure,
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
    if (overflow == Overflow.Scroll) {
      if (flexDirection.isHorizontal) {
        modifier = modifier.horizontalScroll(rememberScrollState())
      } else {
        modifier = modifier.verticalScroll(rememberScrollState())
      }
    }
    testOnlyModifier?.let { modifier = modifier.then(it) }
    return modifier
  }

  private fun measure(
    scope: MeasureScope,
    measurables: List<Measurable>,
    constraints: Constraints,
  ): MeasureResult = with(scope) {
    syncItems(measurables)

    val constrainedWidth = if (constraints.hasFixedWidth) {
      constraints.maxWidth.toFloat()
    } else {
      Size.UNDEFINED
    }
    val constrainedHeight = if (constraints.hasFixedHeight) {
      constraints.maxHeight.toFloat()
    } else {
      Size.UNDEFINED
    }
    rootNode.measure(constrainedWidth, constrainedHeight)

    return layout(rootNode.width.toInt(), rootNode.height.toInt()) {
      for (node in rootNode.children) {
        val placeable = (node.measureCallback as ComposeMeasureCallback).getPlaceable(node)
        placeable.place(node.left.toInt(), node.top.toInt())
      }
    }
  }

  private fun syncItems(measurables: List<Measurable>) {
    rootNode.children.clear()
    measurables.forEachIndexed { index, measurable ->
      val childNode = Node()
      rootNode.children += childNode
      childNode.measureCallback = ComposeMeasureCallback(measurable)
      childNode.applyModifier(children.widgets[index].modifier, density)
    }
  }
}
