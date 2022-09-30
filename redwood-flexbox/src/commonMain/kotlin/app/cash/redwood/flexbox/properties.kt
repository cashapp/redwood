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

package app.cash.redwood.flexbox

import kotlin.jvm.JvmInline
import kotlin.math.abs

/**
 * This attribute controls the alignment of the flex lines in the flex container.
 */
@JvmInline
public value class AlignContent(public val ordinal: Int) {
  init {
    require(ordinal in 0..5) { "unknown AlignContent: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "SpaceBetween"
    4 -> "SpaceAround"
    5 -> "Stretch"
    else -> throw AssertionError()
  }

  public companion object {
    public val FlexStart: AlignContent = AlignContent(0)
    public val FlexEnd: AlignContent = AlignContent(1)
    public val Center: AlignContent = AlignContent(2)
    public val SpaceBetween: AlignContent = AlignContent(3)
    public val SpaceAround: AlignContent = AlignContent(4)
    public val Stretch: AlignContent = AlignContent(5)
  }
}

/**
 * This attribute controls the alignment along the cross axis.
 */
@JvmInline
public value class AlignItems(public val ordinal: Int) {
  init {
    require(ordinal in 0..4) { "unknown AlignItems: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "Baseline"
    4 -> "Stretch"
    else -> throw AssertionError()
  }

  public companion object {
    public val FlexStart: AlignItems = AlignItems(0)
    public val FlexEnd: AlignItems = AlignItems(1)
    public val Center: AlignItems = AlignItems(2)
    public val Baseline: AlignItems = AlignItems(3)
    public val Stretch: AlignItems = AlignItems(4)
  }
}

/**
 * This attribute controls the alignment along the cross axis.
 * The alignment in the same direction can be determined by the
 * [AlignItems] attribute in the parent, but if this is set to
 * other than [AlignSelf.Auto], the cross axis alignment is
 * overridden for this child.
 */
@JvmInline
public value class AlignSelf(public val ordinal: Int) {
  init {
    require(ordinal in 0..5) { "unknown AlignSelf: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "Baseline"
    4 -> "Stretch"
    5 -> "Auto"
    else -> throw AssertionError()
  }

  public companion object {
    public val FlexStart: AlignSelf = AlignSelf(0)
    public val FlexEnd: AlignSelf = AlignSelf(1)
    public val Center: AlignSelf = AlignSelf(2)
    public val Baseline: AlignSelf = AlignSelf(3)
    public val Stretch: AlignSelf = AlignSelf(4)
    public val Auto: AlignSelf = AlignSelf(5)
  }
}

/**
 * The direction children items are placed inside the flex container, it determines the
 * direction of the main axis (and the cross axis, perpendicular to the main axis).
 */
@JvmInline
public value class FlexDirection(public val ordinal: Int) {
  init {
    require(ordinal in 0..3) { "unknown FlexDirection: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "Row"
    1 -> "RowReverse"
    2 -> "Column"
    3 -> "ColumnReverse"
    else -> throw AssertionError()
  }

  public companion object {
    public val Row: FlexDirection = FlexDirection(0)
    public val RowReverse: FlexDirection = FlexDirection(1)
    public val Column: FlexDirection = FlexDirection(2)
    public val ColumnReverse: FlexDirection = FlexDirection(3)
  }
}

/**
 * Returns `true` if this direction's main axis is horizontal.
 */
public val FlexDirection.isHorizontal: Boolean
  get() = this == FlexDirection.Row || this == FlexDirection.RowReverse

/**
 * This attribute controls whether the flex container is single-line or multi-line,
 * and the direction of the cross axis.
 */
@JvmInline
public value class FlexWrap(public val ordinal: Int) {
  init {
    require(ordinal in 0..2) { "unknown FlexWrap: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "NoWrap"
    1 -> "Wrap"
    2 -> "WrapReverse"
    else -> throw AssertionError()
  }

  public companion object {
    public val NoWrap: FlexWrap = FlexWrap(0)
    public val Wrap: FlexWrap = FlexWrap(1)
    public val WrapReverse: FlexWrap = FlexWrap(2)
  }
}

/**
 * This attribute controls the alignment along the main axis.
 */
@JvmInline
public value class JustifyContent(public val ordinal: Int) {
  init {
    require(ordinal in 0..5) { "unknown JustifyContent: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "SpaceBetween"
    4 -> "SpaceAround"
    5 -> "SpaceEvenly"
    else -> throw AssertionError()
  }

  public companion object {
    public val FlexStart: JustifyContent = JustifyContent(0)
    public val FlexEnd: JustifyContent = JustifyContent(1)
    public val Center: JustifyContent = JustifyContent(2)
    public val SpaceBetween: JustifyContent = JustifyContent(3)
    public val SpaceAround: JustifyContent = JustifyContent(4)
    public val SpaceEvenly: JustifyContent = JustifyContent(5)
  }
}

/**
 * A MeasureSpec encapsulates the layout requirements passed from parent to child.
 * Each MeasureSpec represents a requirement for either the width or the height.
 * A MeasureSpec is comprised of a size and a mode.
 */
@JvmInline
public value class MeasureSpec private constructor(internal val value: Int) {

  public val size: Int get() = value and 0x3FFF
  public val mode: MeasureSpecMode get() = MeasureSpecMode(abs(value shr 30))

  public companion object {
    public const val MaxSize: Int = Int.MAX_VALUE and 0x00FFFFFF

    public fun from(size: Int, mode: MeasureSpecMode): MeasureSpec {
      require(size in 0..MaxSize) { "invalid size: $size" }
      // Use the top 2 bits for the mode and use the bottom 30 bits for the size.
      return MeasureSpec((mode.ordinal shl 30) or (size and 0x3FFF))
    }

    /** A convenience function to constrain [size] inside a given [measureSpec]. */
    public fun resolveSize(size: Int, measureSpec: MeasureSpec): Int {
      return when (measureSpec.mode) {
        MeasureSpecMode.AtMost -> minOf(measureSpec.size, size)
        MeasureSpecMode.Exactly -> measureSpec.size
        MeasureSpecMode.Unspecified -> size
        else -> throw AssertionError()
      }
    }
  }
}

/**
 * Denotes how the [MeasureSpec.size] constraint should be interpreted.
 */
@JvmInline
public value class MeasureSpecMode(public val ordinal: Int) {
  init {
    require(ordinal in 0..2) { "unknown MeasureSpecMode: $ordinal" }
  }

  override fun toString(): String = when (ordinal) {
    0 -> "Unspecified"
    1 -> "Exactly"
    2 -> "AtMost"
    else -> throw AssertionError()
  }

  public companion object {
    /**
     * The parent has not imposed any constraint on the child. It can be whatever size it wants.
     */
    public val Unspecified: MeasureSpecMode = MeasureSpecMode(0)

    /**
     * The parent has determined an exact size for the child. The child is going to be
     * given those bounds regardless of how big it wants to be.
     */
    public val Exactly: MeasureSpecMode = MeasureSpecMode(1)

    /**
     * The child can be as large as it wants up to the specified size.
     */
    public val AtMost: MeasureSpecMode = MeasureSpecMode(2)
  }
}

/**
 * Describes the padding/margin to apply to a node/flexbox.
 */
public data class Spacing(
  val start: Int = 0,
  val end: Int = 0,
  val top: Int = 0,
  val bottom: Int = 0,
) {
  init {
    require(start >= 0 && end >= 0 && top >= 0 && bottom >= 0) {
      "invalid Spacing: [$start, $end, $top, $bottom]"
    }
  }

  public companion object {
    public val Zero: Spacing = Spacing()
  }
}

/**
 * A two-dimensional size composed of two [Int]s.
 */
public data class Size(
  val width: Int,
  val height: Int,
) {
  init {
    require(width >= 0 && height >= 0) {
      "invalid Size: [$width, $height]"
    }
  }

  public companion object {
    public val Zero: Size = Size(0, 0)
  }
}
