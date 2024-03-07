/*
 * Copyright 2019 The Android Open Source Project
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastForEachIndexed
import kotlin.math.max

/**
 * A custom [androidx.compose.foundation.layout.Box] implementation that:
 *
 * - Supports passing child layout info as part of the Box's constructor instead of reading the
 *   information from the child's modifier.
 * - Supports stretching children along each axis individually.
 */
@Composable
internal inline fun Box(
  noinline childLayoutInfo: (index: Int) -> BoxChildLayoutInfo,
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  propagateMinConstraints: Boolean = false,
  content: @Composable () -> Unit,
) {
  val measurePolicy = remember(contentAlignment, propagateMinConstraints) {
    BoxMeasurePolicy(childLayoutInfo, contentAlignment, propagateMinConstraints)
  }
  Layout(
    content = content,
    measurePolicy = measurePolicy,
    modifier = modifier,
  )
}

@PublishedApi
internal data class BoxMeasurePolicy(
  private val childLayoutInfo: (index: Int) -> BoxChildLayoutInfo,
  private val alignment: Alignment,
  private val propagateMinConstraints: Boolean,
) : MeasurePolicy {
  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints,
  ): MeasureResult {
    if (measurables.isEmpty()) {
      return layout(
        constraints.minWidth,
        constraints.minHeight,
      ) {}
    }

    val contentConstraints = if (propagateMinConstraints) {
      constraints
    } else {
      constraints.copy(minWidth = 0, minHeight = 0)
    }

    if (measurables.size == 1) {
      val measurable = measurables[0]
      val boxWidth: Int
      val boxHeight: Int
      val placeable: Placeable
      if (!measurable.matchesParentSize) {
        placeable = measurable.measure(contentConstraints)
        boxWidth = max(constraints.minWidth, placeable.width)
        boxHeight = max(constraints.minHeight, placeable.height)
      } else {
        boxWidth = constraints.minWidth
        boxHeight = constraints.minHeight
        placeable = measurable.measure(
          Constraints.fixed(constraints.minWidth, constraints.minHeight),
        )
      }
      return layout(boxWidth, boxHeight) {
        placeInBox(placeable, measurable, layoutDirection, boxWidth, boxHeight, alignment)
      }
    }

    val placeables = arrayOfNulls<Placeable>(measurables.size)
    // First measure non match parent size children to get the size of the Box.
    var hasMatchParentSizeChildren = false
    var boxWidth = constraints.minWidth
    var boxHeight = constraints.minHeight
    measurables.fastForEachIndexed { index, measurable ->
      if (!measurable.matchesParentSize) {
        val placeable = measurable.measure(contentConstraints)
        placeables[index] = placeable
        boxWidth = max(boxWidth, placeable.width)
        boxHeight = max(boxHeight, placeable.height)
      } else {
        hasMatchParentSizeChildren = true
      }
    }

    // Now measure match parent size children, if any.
    if (hasMatchParentSizeChildren) {
      // The infinity check is needed for default intrinsic measurements.
      val matchParentSizeConstraints = Constraints(
        minWidth = if (boxWidth != Constraints.Infinity) boxWidth else 0,
        minHeight = if (boxHeight != Constraints.Infinity) boxHeight else 0,
        maxWidth = boxWidth,
        maxHeight = boxHeight,
      )
      measurables.fastForEachIndexed { index, measurable ->
        if (measurable.matchesParentSize) {
          placeables[index] = measurable.measure(matchParentSizeConstraints)
        }
      }
    }

    // Specify the size of the Box and position its children.
    return layout(boxWidth, boxHeight) {
      placeables.forEachIndexed { index, placeable ->
        placeable as Placeable
        val measurable = measurables[index]
        placeInBox(placeable, measurable, layoutDirection, boxWidth, boxHeight, alignment)
      }
    }
  }
}

private fun Placeable.PlacementScope.placeInBox(
  placeable: Placeable,
  measurable: Measurable,
  layoutDirection: LayoutDirection,
  boxWidth: Int,
  boxHeight: Int,
  alignment: Alignment,
) {
  val childAlignment = measurable.boxChildDataNode?.alignment ?: alignment
  val position = childAlignment.align(
    IntSize(placeable.width, placeable.height),
    IntSize(boxWidth, boxHeight),
    layoutDirection,
  )
  placeable.place(position)
}

private val Measurable.boxChildDataNode: BoxChildDataNode? get() = parentData as? BoxChildDataNode
private val Measurable.matchesParentSize: Boolean get() = matchesParentWidth && matchesParentHeight
private val Measurable.matchesParentWidth: Boolean get() = boxChildDataNode?.matchParentWidth ?: false
private val Measurable.matchesParentHeight: Boolean get() = boxChildDataNode?.matchParentHeight ?: false

private class BoxChildDataNode(
  var alignment: Alignment,
  var matchParentWidth: Boolean,
  var matchParentHeight: Boolean,
) : ParentDataModifierNode, Modifier.Node() {
  override fun Density.modifyParentData(parentData: Any?) = this@BoxChildDataNode
}

internal class BoxChildLayoutInfo(
  val alignment: Alignment,
  val matchParentWidth: Boolean,
  val matchParentHeight: Boolean,
)
