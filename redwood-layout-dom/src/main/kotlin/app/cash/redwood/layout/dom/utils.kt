/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.layout.dom

import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import kotlin.math.roundToInt

// TODO: Figure out a good density multiple to map units into a reasonable size on web.
internal const val DensityMultiplier = 1.0

internal fun unitsToPx(units: Int): String {
  return "${(DensityMultiplier * units).roundToInt()}px"
}

internal fun MainAxisAlignment.toCss() = when (this) {
  MainAxisAlignment.Start -> "start"
  MainAxisAlignment.Center -> "center"
  MainAxisAlignment.End -> "end"
  MainAxisAlignment.SpaceBetween -> "space-between"
  MainAxisAlignment.SpaceAround -> "space-around"
  MainAxisAlignment.SpaceEvenly -> "space-evenly"
  else -> throw AssertionError()
}

internal fun CrossAxisAlignment.toCss() = when (this) {
  CrossAxisAlignment.Start -> "start"
  CrossAxisAlignment.Center -> "center"
  CrossAxisAlignment.End -> "end"
  CrossAxisAlignment.Stretch -> "stretch"
  else -> throw AssertionError()
}

internal fun Overflow.toCss() = when (this) {
  Overflow.Clip -> "hidden"
  Overflow.Scroll -> "scroll"
  else -> throw AssertionError()
}
