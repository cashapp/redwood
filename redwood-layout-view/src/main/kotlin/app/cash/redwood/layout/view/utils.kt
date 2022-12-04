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
import android.view.View
import app.cash.redwood.LayoutModifier
import app.cash.redwood.flexbox.AlignSelf
import app.cash.redwood.flexbox.FlexDirection
import app.cash.redwood.flexbox.FlexItem
import app.cash.redwood.flexbox.FlexItem.Companion.DefaultFlexGrow
import app.cash.redwood.flexbox.FlexItem.Companion.DefaultFlexShrink
import app.cash.redwood.flexbox.Measurable
import app.cash.redwood.flexbox.MeasureSpec
import app.cash.redwood.flexbox.MeasureSpecMode
import app.cash.redwood.flexbox.Size
import app.cash.redwood.flexbox.Spacing
import app.cash.redwood.flexbox.isHorizontal
import app.cash.redwood.flexbox.isVertical
import app.cash.redwood.layout.Grow
import app.cash.redwood.layout.HorizontalAlignment
import app.cash.redwood.layout.Padding as PaddingModifier
import app.cash.redwood.layout.Shrink
import app.cash.redwood.layout.VerticalAlignment
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.Padding

internal fun Padding.toSpacing(context: Context): Spacing {
  val density = DensityMultiplier * context.resources.displayMetrics.density
  return Spacing(
    start = density * start.toDouble(),
    end = density * end.toDouble(),
    top = density * top.toDouble(),
    bottom = density * bottom.toDouble(),
  )
}

// Android uses 2.75 as a density scale for most recent Pixel devices and iOS
// uses 3. This aligns the two so the generic values used by Redwood layout are
// visually similar on both platforms.
private const val DensityMultiplier = 1.1

internal fun MeasureSpec.Companion.fromAndroid(measureSpec: Int): MeasureSpec = from(
  size = View.MeasureSpec.getSize(measureSpec).toDouble(),
  mode = MeasureSpecMode.fromAndroid(View.MeasureSpec.getMode(measureSpec)),
)

internal fun MeasureSpec.toAndroid(): Int = View.MeasureSpec.makeMeasureSpec(size.toInt(), mode.toAndroid())

internal fun MeasureSpecMode.Companion.fromAndroid(mode: Int): MeasureSpecMode = when (mode) {
  View.MeasureSpec.UNSPECIFIED -> Unspecified
  View.MeasureSpec.EXACTLY -> Exactly
  View.MeasureSpec.AT_MOST -> AtMost
  else -> throw AssertionError()
}

internal fun MeasureSpecMode.toAndroid(): Int = when (this) {
  MeasureSpecMode.Unspecified -> View.MeasureSpec.UNSPECIFIED
  MeasureSpecMode.Exactly -> View.MeasureSpec.EXACTLY
  MeasureSpecMode.AtMost -> View.MeasureSpec.AT_MOST
  else -> throw AssertionError()
}

internal fun newFlexItem(
  context: Context,
  direction: FlexDirection,
  layoutModifiers: LayoutModifier,
  measurable: Measurable,
): FlexItem {
  var flexGrow = DefaultFlexGrow
  var flexShrink = DefaultFlexShrink
  var padding = Padding.Zero
  var crossAxisAlignment = CrossAxisAlignment.Start
  var isCrossAxisAlignmentSet = false
  layoutModifiers.forEach { modifier ->
    when (modifier) {
      is Grow -> flexGrow = modifier.value
      is Shrink -> flexShrink = modifier.value
      is PaddingModifier -> padding = modifier.padding
      is HorizontalAlignment -> if (direction.isVertical) {
        crossAxisAlignment = modifier.alignment
        isCrossAxisAlignmentSet = true
      }
      is VerticalAlignment -> if (direction.isHorizontal) {
        crossAxisAlignment = modifier.alignment
        isCrossAxisAlignmentSet = true
      }
    }
  }
  return FlexItem(
    flexGrow = flexGrow,
    flexShrink = flexShrink,
    margin = padding.toSpacing(context),
    alignSelf = if (isCrossAxisAlignmentSet) {
      crossAxisAlignment.toAlignSelf()
    } else {
      AlignSelf.Auto
    },
    measurable = measurable,
  )
}

internal class ViewMeasurable(val view: View) : Measurable() {
  override val requestedWidth get() = view.layoutParams.width.toDouble()
  override val requestedHeight get() = view.layoutParams.height.toDouble()
  override val minWidth get() = view.minimumWidth.toDouble()
  override val minHeight get() = view.minimumHeight.toDouble()

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    view.measure(widthSpec.toAndroid(), heightSpec.toAndroid())
    return Size(view.measuredWidth.toDouble(), view.measuredHeight.toDouble())
  }
}
