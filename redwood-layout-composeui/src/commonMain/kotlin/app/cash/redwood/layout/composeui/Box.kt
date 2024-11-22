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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
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
  childrenLayoutInfo: BoxChildrenLayoutInfo,
  modifier: Modifier = Modifier,
  propagateMinConstraints: Boolean = false,
  content: @Composable () -> Unit,
) {
  val measurePolicy = remember(childrenLayoutInfo, propagateMinConstraints) {
    BoxMeasurePolicy(childrenLayoutInfo.infos, propagateMinConstraints)
  }
  Layout(
    content = content,
    measurePolicy = measurePolicy,
    modifier = modifier,
  )
}

/** Wrapper class to ensure argument stability when passed to a Compose function. */
@Immutable
internal data class BoxChildrenLayoutInfo(
  val infos: List<BoxChildLayoutInfo>,
)

@Immutable
internal data class BoxChildLayoutInfo(
  val alignment: Alignment,
  val matchParentWidth: Boolean,
  val matchParentHeight: Boolean,
)

@PublishedApi
internal data class BoxMeasurePolicy(
  private val childrenLayoutInfo: List<BoxChildLayoutInfo>,
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

    val placeables = arrayOfNulls<Placeable>(measurables.size)
    var boxWidth = constraints.minWidth
    var boxHeight = constraints.minHeight
    measurables.fastForEachIndexed { index, measurable ->
      var childConstraints = contentConstraints
      val layoutInfo = childrenLayoutInfo[index]

      if (layoutInfo.matchParentWidth) {
        childConstraints = childConstraints.copy(
          minWidth = if (boxWidth != Constraints.Infinity) boxWidth else 0,
          maxWidth = boxWidth,
        )
      }
      if (layoutInfo.matchParentHeight) {
        childConstraints = childConstraints.copy(
          minHeight = if (boxHeight != Constraints.Infinity) boxHeight else 0,
          maxHeight = boxHeight,
        )
      }
      val placeable = measurable.measure(childConstraints)
      placeables[index] = placeable
      boxWidth = max(boxWidth, placeable.width)
      boxHeight = max(boxHeight, placeable.height)
    }

    // Specify the size of the Box and position its children.
    return layout(boxWidth, boxHeight) {
      placeables.forEachIndexed { index, placeable ->
        placeInBox(placeable!!, layoutDirection, boxWidth, boxHeight, childrenLayoutInfo[index])
      }
    }
  }
}

private fun Placeable.PlacementScope.placeInBox(
  placeable: Placeable,
  layoutDirection: LayoutDirection,
  boxWidth: Int,
  boxHeight: Int,
  layoutInfo: BoxChildLayoutInfo,
) {
  val position = layoutInfo.alignment.align(
    IntSize(placeable.width, placeable.height),
    IntSize(boxWidth, boxHeight),
    layoutDirection,
  )
  placeable.place(position)
}
