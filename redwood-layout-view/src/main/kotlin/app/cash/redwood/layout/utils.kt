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
@file:JvmName("view_utils")

package app.cash.redwood.layout

import android.view.View
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.FlexItem
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.flexcontainer.Measurable
import app.cash.redwood.flexcontainer.MeasureSpec
import app.cash.redwood.flexcontainer.MeasureSpecMode
import app.cash.redwood.flexcontainer.Size
import app.cash.redwood.flexcontainer.Spacing
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Padding

internal fun MainAxisAlignment.toJustifyContent() = when (this) {
  MainAxisAlignment.Start -> JustifyContent.FlexStart
  MainAxisAlignment.Center -> JustifyContent.Center
  MainAxisAlignment.End -> JustifyContent.FlexEnd
  MainAxisAlignment.SpaceBetween -> JustifyContent.SpaceBetween
  MainAxisAlignment.SpaceAround -> JustifyContent.SpaceAround
  MainAxisAlignment.SpaceEvenly -> JustifyContent.SpaceEvenly
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toAlignItems() = when (this) {
  CrossAxisAlignment.Start -> AlignItems.FlexStart
  CrossAxisAlignment.Center -> AlignItems.Center
  CrossAxisAlignment.End -> AlignItems.FlexEnd
  CrossAxisAlignment.Stretch -> AlignItems.Stretch
  else -> throw AssertionError()
}

internal fun Padding.toSpacing() = Spacing(start, end, top, bottom)

internal fun MeasureSpec.Companion.fromAndroid(measureSpec: Int): MeasureSpec = from(
  size = View.MeasureSpec.getSize(measureSpec),
  mode = MeasureSpecMode.fromAndroid(View.MeasureSpec.getMode(measureSpec)),
)

internal fun MeasureSpec.toAndroid(): Int = View.MeasureSpec.makeMeasureSpec(size, mode.toAndroid())

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

internal fun View.asItem() = FlexItem(measurable = ViewMeasurable(this@asItem))

private class ViewMeasurable(private val view: View) : Measurable() {
  override val requestedWidth get() = view.layoutParams.width
  override val requestedHeight get() = view.layoutParams.height
  override val minWidth get() = view.minimumWidth
  override val minHeight get() = view.minimumHeight

  override fun width(height: Int) = measure(
    widthSpec = MeasureSpec.from(0, MeasureSpecMode.Unspecified),
    heightSpec = MeasureSpec.from(height, MeasureSpecMode.Exactly)
  ).width

  override fun height(width: Int) = measure(
    widthSpec = MeasureSpec.from(width, MeasureSpecMode.Exactly),
    heightSpec = MeasureSpec.from(0, MeasureSpecMode.Unspecified)
  ).height

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    view.measure(widthSpec.toAndroid(), heightSpec.toAndroid())
    return Size(view.measuredWidth, view.measuredHeight)
  }
}
