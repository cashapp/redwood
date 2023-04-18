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
@file:Suppress("MemberVisibilityCanBePrivate")

package app.cash.redwood.layout.api

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

// TODO make value classes have private ctors and private vals.
//  Blocked by https://issuetracker.google.com/issues/251430194.

/** Controls how the container should determine its width/height. */
@JvmInline
@Serializable
public value class Constraint internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Wrap"
    1 -> "Fill"
    else -> throw AssertionError()
  }

  public companion object {
    public val Wrap: Constraint = Constraint(0)
    public val Fill: Constraint = Constraint(1)
  }
}

/** Equivalent to `justify-content`. */
@JvmInline
@Serializable
public value class MainAxisAlignment internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Start"
    1 -> "Center"
    2 -> "End"
    3 -> "SpaceBetween"
    4 -> "SpaceAround"
    5 -> "SpaceEvenly"
    else -> throw AssertionError()
  }

  public companion object {
    public val Start: MainAxisAlignment = MainAxisAlignment(0)
    public val Center: MainAxisAlignment = MainAxisAlignment(1)
    public val End: MainAxisAlignment = MainAxisAlignment(2)
    public val SpaceBetween: MainAxisAlignment = MainAxisAlignment(3)
    public val SpaceAround: MainAxisAlignment = MainAxisAlignment(4)
    public val SpaceEvenly: MainAxisAlignment = MainAxisAlignment(5)
  }
}

/** Equivalent to `align-items`. */
@JvmInline
@Serializable
public value class CrossAxisAlignment internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Start"
    1 -> "Center"
    2 -> "End"
    3 -> "Stretch"
    else -> throw AssertionError()
  }

  public companion object {
    public val Start: CrossAxisAlignment = CrossAxisAlignment(0)
    public val Center: CrossAxisAlignment = CrossAxisAlignment(1)
    public val End: CrossAxisAlignment = CrossAxisAlignment(2)
    public val Stretch: CrossAxisAlignment = CrossAxisAlignment(3)
  }
}

/** Equivalent to `overflow-x`/`overflow-y`. */
@JvmInline
@Serializable
public value class Overflow internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Clip"
    1 -> "Scroll"
    else -> throw AssertionError()
  }

  public companion object {
    public val Clip: Overflow = Overflow(0)
    public val Scroll: Overflow = Overflow(1)
  }
}

@Immutable
@Serializable
public data class Margin(
  val left: Int = 0,
  val right: Int = 0,
  val top: Int = 0,
  val bottom: Int = 0,
) {
  init {
    require(left >= 0 && right >= 0 && top >= 0 && bottom >= 0) {
      "Invalid Margin: [$left, $right, $top, $bottom]"
    }
  }

  override fun toString(): String {
    return if (left == right && top == bottom) {
      if (left == top) {
        "Margin(all=$left)"
      } else {
        "Margin(horizontal=$left, vertical=$top)"
      }
    } else {
      "Margin(left=$left, right=$right, top=$top, bottom=$bottom)"
    }
  }

  public companion object {
    public val Zero: Margin = Margin()
  }
}

@Stable
public fun Margin(
  horizontal: Int = 0,
  vertical: Int = 0,
): Margin = Margin(horizontal, horizontal, vertical, vertical)

@Stable
public fun Margin(
  all: Int = 0,
): Margin = Margin(all, all, all, all)

// Temporary constructor before migrating everything to doubles.
@Stable
public fun Margin(
  left: Double = 0.0,
  right: Double = 0.0,
  top: Double = 0.0,
  bottom: Double = 0.0,
): Margin = Margin(
  left = left.toInt(),
  right = right.toInt(),
  top = top.toInt(),
  bottom = bottom.toInt(),
)
