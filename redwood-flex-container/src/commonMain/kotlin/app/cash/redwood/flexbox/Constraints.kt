/*
 * Copyright 2016 Google Inc. All rights reserved.
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
package app.cash.redwood.flexbox

/**
 * Represents the measurement constraints for a [Measurable].
 */
public data class Constraints(
  val minWidth: Double = 0.0,
  val maxWidth: Double = Infinity,
  val minHeight: Double = 0.0,
  val maxHeight: Double = Infinity,
) {
  init {
    require(minWidth in 0.0..maxWidth && minHeight in 0.0..maxHeight) {
      "Invalid Constraints: [$minWidth, $maxWidth, $minHeight, $maxHeight]"
    }
  }

  public companion object {
    public const val Infinity: Double = Double.MAX_VALUE

    /** Create [Constraints] with a fixed [width]. */
    public fun fixedWidth(width: Double): Constraints {
      return Constraints(minWidth = width, maxWidth = width)
    }

    /** Create [Constraints] with a fixed [height]. */
    public fun fixedHeight(height: Double): Constraints {
      return Constraints(minHeight = height, maxHeight = height)
    }

    /** Create [Constraints] with a fixed [width] and [height]. */
    public fun fixed(width: Double, height: Double): Constraints {
      return Constraints(width, width, height, height)
    }
  }
}

/** Returns true if the constraints have a finite maximum width. */
public val Constraints.hasBoundedWidth: Boolean
  get() = maxWidth != Constraints.Infinity

/** Returns true if the constraints have a finite maximum height. */
public val Constraints.hasBoundedHeight: Boolean
  get() = maxHeight != Constraints.Infinity

/** Returns true if the constraints enforce an exact width. */
public val Constraints.hasFixedWidth: Boolean
  get() = minWidth == maxWidth

/** Returns true if the constraints enforce an exact height. */
public val Constraints.hasFixedHeight: Boolean
  get() = minHeight == maxHeight

/** Returns a copy of [size] that satisfies the constraints. */
public fun Constraints.constrain(size: Size): Size = Size(
  width = size.width.coerceIn(minWidth, maxWidth),
  height = size.height.coerceIn(minHeight, maxHeight),
)

/** Constrains [other] so that it satisfies the constraints and returns the result. */
public fun Constraints.constrain(other: Constraints): Constraints = Constraints(
  minWidth = other.minWidth.coerceIn(minWidth, maxWidth),
  maxWidth = other.maxWidth.coerceIn(minWidth, maxWidth),
  minHeight = other.minHeight.coerceIn(minHeight, maxHeight),
  maxHeight = other.maxHeight.coerceIn(minHeight, maxHeight),
)

/** Constrains [width] so that it satisfies the width constraints and returns the result. */
public fun Constraints.constrainWidth(width: Double): Double {
  return width.coerceIn(minWidth, maxWidth)
}

/** Constrains [height] so that it satisfies the height constraints and returns the result. */
public fun Constraints.constrainHeight(height: Double): Double {
  return height.coerceIn(minHeight, maxHeight)
}
