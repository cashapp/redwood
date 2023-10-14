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
package app.cash.redwood.ui

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable

/**
 * Represents a device's display density.
 *
 * @param rawDensity The raw, platform-specific density which hasn't been
 *  normalized with respect to other platforms.
 */
@[Immutable JvmInline Serializable]
public value class Density(
  public val rawDensity: Double,
) {
  init {
    require(rawDensity > 0) {
      "rawDensity must be positive: $rawDensity"
    }
  }

  /** Convert a [Dp] into a pixel value. */
  public fun Dp.toPx(): Double {
    return value * DensityMultiplier * rawDensity
  }

  /** Convert a [Dp] into the nearest integer pixel value. */
  public fun Dp.toPxInt(): Int {
    return toPx().roundToInt()
  }

  /** Convert a pixel value into a [Dp]. */
  public fun Int.toDp(): Dp {
    return Dp(this / DensityMultiplier / rawDensity)
  }

  /** Convert a pixel value into a [Dp]. */
  public fun Float.toDp(): Dp {
    return Dp(this / DensityMultiplier / rawDensity)
  }

  /** Convert a pixel value into a [Dp]. */
  public fun Double.toDp(): Dp {
    return Dp(this / DensityMultiplier / rawDensity)
  }

  /** Empty companion object used for extensions. */
  public companion object
}

/**
 * A multiplier that's used to normalize the density values of each
 * target platform so [Dp] values look similar on each device.
 */
internal expect val DensityMultiplier: Double
