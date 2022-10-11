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

import androidx.compose.ui.unit.Constraints
import app.cash.redwood.flexcontainer.AlignItems
import app.cash.redwood.flexcontainer.JustifyContent
import app.cash.redwood.flexcontainer.MeasureSpec
import app.cash.redwood.flexcontainer.MeasureSpecMode
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

internal fun Constraints.toMeasureSpecs(): Pair<MeasureSpec, MeasureSpec> {
  val widthSpec = when {
    hasFixedWidth -> MeasureSpec.from(maxWidth, MeasureSpecMode.Exactly)
    hasBoundedWidth -> MeasureSpec.from(maxWidth, MeasureSpecMode.AtMost)
    else -> MeasureSpec.from(minWidth, MeasureSpecMode.Unspecified)
  }
  val heightSpec = when {
    hasFixedHeight -> MeasureSpec.from(maxHeight, MeasureSpecMode.Exactly)
    hasBoundedHeight -> MeasureSpec.from(maxHeight, MeasureSpecMode.AtMost)
    else -> MeasureSpec.from(minHeight, MeasureSpecMode.Unspecified)
  }
  return widthSpec to heightSpec
}

internal fun Pair<MeasureSpec, MeasureSpec>.toConstraints(): Constraints {
  val (widthSpec, heightSpec) = this
  val minWidth: Int
  val maxWidth: Int
  when (widthSpec.mode) {
    MeasureSpecMode.Exactly -> {
      minWidth = widthSpec.size
      maxWidth = widthSpec.size
    }
    MeasureSpecMode.AtMost -> {
      minWidth = 0
      maxWidth = widthSpec.size
    }
    MeasureSpecMode.Unspecified -> {
      minWidth = 0
      maxWidth = Constraints.Infinity
    }
    else -> throw AssertionError()
  }
  val minHeight: Int
  val maxHeight: Int
  when (heightSpec.mode) {
    MeasureSpecMode.Exactly -> {
      minHeight = heightSpec.size
      maxHeight = heightSpec.size
    }
    MeasureSpecMode.AtMost -> {
      minHeight = 0
      maxHeight = heightSpec.size
    }
    MeasureSpecMode.Unspecified -> {
      minHeight = 0
      maxHeight = Constraints.Infinity
    }
    else -> throw AssertionError()
  }
  return Constraints(minWidth, maxWidth, minHeight, maxHeight)
}
