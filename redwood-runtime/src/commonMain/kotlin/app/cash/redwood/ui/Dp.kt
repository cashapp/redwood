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
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/**
 * Represents a density-independent pixel value. Values will be scaled and
 * converted into standard pixel values according to the device's density.
 */
@[Immutable JvmInline Serializable]
public value class Dp(
  public val value: Double,
) {
  init {
    require(value >= 0) {
      "value must be non-negative: $value"
    }
  }

  override fun toString(): String = "$value.dp"

  /** Empty companion object used for extensions. */
  public companion object
}

/** Create a [Dp] from an [Int]. */
@Stable
public inline val Int.dp: Dp get() = Dp(toDouble())

/** Create a [Dp] from a [Float]. */
@Stable
public inline val Float.dp: Dp get() = Dp(toDouble())

/** Create a [Dp] from a [Double]. */
@Stable
public inline val Double.dp: Dp get() = Dp(toDouble())

/**
 * Convert device-agnostic density-independent pixel value into a
 * device-specific density-independent pixel value.
 */
public fun Dp.toPlatformDp(): Double {
  return value / DensityMultiplier
}

/**
 * Convert device-specific density-independent pixel value into a
 * device-agnostic density-independent pixel value.
 */
public fun Dp.Companion.fromPlatformDp(value: Double): Dp {
  return Dp(value * DensityMultiplier)
}
