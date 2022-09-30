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

package app.cash.redwood.layout

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

/** Equivalent to `justify-content`. */
@JvmInline
@Serializable
public value class MainAxisAlignment(public val ordinal: Int) {
  init {
    require(ordinal in 0..5) { "Unknown MainAxisAlignment: $ordinal" }
  }

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
public value class CrossAxisAlignment(public val ordinal: Int) {
  init {
    require(ordinal in 0..3) { "Unknown CrossAxisAlignment: $ordinal" }
  }

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
public value class Overflow(public val ordinal: Int) {
  init {
    require(ordinal in 0..1) { "Unknown Overflow: $ordinal" }
  }

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
public data class Padding(
  val start: Int = 0,
  val end: Int = 0,
  val top: Int = 0,
  val bottom: Int = 0,
) {
  init {
    require(start >= 0 && end >= 0 && top >= 0 && bottom >= 0) {
      "Invalid Padding: [$start, $end, $top, $bottom]"
    }
  }

  public companion object {
    public val Zero: Padding = Padding()
  }
}

@Stable
public fun Padding(
  horizontal: Int = 0,
  vertical: Int = 0,
): Padding = Padding(horizontal, horizontal, vertical, vertical)

@Stable
public fun Padding(
  value: Int = 0,
): Padding = Padding(value, value, value, value)
