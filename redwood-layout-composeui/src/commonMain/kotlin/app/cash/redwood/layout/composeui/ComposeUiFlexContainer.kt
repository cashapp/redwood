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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignItems
import app.cash.redwood.flexbox.FlexContainer
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.JustifyContent
import app.cash.redwood.flexbox.isHorizontal
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.Density
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Margin
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.widget.compose.ComposeWidgetChildren

internal class ComposeUiFlexContainer(
  private val direction: FlexDirection,
) : Row<@Composable () -> Unit>, Column<@Composable () -> Unit> {
  private val container = FlexContainer().apply {
    flexDirection = direction
    roundToInt = true
  }

  override val children = ComposeWidgetChildren()

  override var layoutModifiers: LayoutModifier = LayoutModifier

  private var recomposeTick by mutableStateOf(0)
  private var overflow by mutableStateOf(Overflow.Clip)
  private var margin by mutableStateOf(Margin.Zero)

  private var density = Density(1.0)
  private var marginUpdated = false

  var modifier: Modifier by mutableStateOf(Modifier)

  override fun width(width: Constraint) {
    container.fillWidth = width == Constraint.Fill
    invalidate()
  }

  override fun height(height: Constraint) {
    container.fillHeight = height == Constraint.Fill
    invalidate()
  }

  override fun margin(margin: Margin) {
    this.marginUpdated = true
    this.margin = margin
  }

  override fun overflow(overflow: Overflow) {
    this.overflow = overflow
  }

  override fun horizontalAlignment(horizontalAlignment: MainAxisAlignment) {
    justifyContent(horizontalAlignment.toJustifyContent())
  }

  override fun horizontalAlignment(horizontalAlignment: CrossAxisAlignment) {
    alignItems(horizontalAlignment.toAlignItems())
  }

  override fun verticalAlignment(verticalAlignment: MainAxisAlignment) {
    justifyContent(verticalAlignment.toJustifyContent())
  }

  override fun verticalAlignment(verticalAlignment: CrossAxisAlignment) {
    alignItems(verticalAlignment.toAlignItems())
  }

  fun alignItems(alignItems: AlignItems) {
    container.alignItems = alignItems
    invalidate()
  }

  fun justifyContent(justifyContent: JustifyContent) {
    container.justifyContent = justifyContent
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

        // Read the density for use in 'measure'.
        updateDensity(Density(LocalDensity.current.density.toDouble()))

        children.render()
      },
      modifier = if (overflow == Overflow.Scroll) {
        if (direction.isHorizontal) {
          modifier.horizontalScroll(rememberScrollState())
        } else {
          modifier.verticalScroll(rememberScrollState())
        }
      } else {
        modifier
      },
      measurePolicy = ::measure,
    )
  }

  private fun updateDensity(density: Density) {
    if (density != this.density || marginUpdated) {
      this.density = density
      this.marginUpdated = false
      container.margin = margin.toSpacing(density)
    }
  }

  private fun measure(
    scope: MeasureScope,
    measurables: List<Measurable>,
    constraints: Constraints,
  ): MeasureResult = with(scope) {
    syncItems(measurables)

    val (widthSpec, heightSpec) = constraints.toMeasureSpecs()
    val (width, height) = container.measure(widthSpec, heightSpec)

    return layout(width.toInt(), height.toInt()) {
      for (item in container.items) {
        val placeable = (item.measurable as ComposeMeasurable).placeable
        placeable.place(item.left.toInt(), item.top.toInt())
      }
    }
  }

  private fun syncItems(measurables: List<Measurable>) {
    container.items.clear()
    measurables.forEachIndexed { index, measurable ->
      container.items += newFlexItem(
        direction = direction,
        density = density,
        layoutModifiers = children.widgets[index].layoutModifiers,
        measurable = ComposeMeasurable(measurable),
      )
    }
  }
}
