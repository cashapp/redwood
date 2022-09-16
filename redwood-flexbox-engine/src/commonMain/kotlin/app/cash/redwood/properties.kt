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
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package app.cash.redwood

import kotlin.jvm.JvmInline
import kotlin.math.abs

/**
 * This attribute controls the alignment of the flex lines in the flex container.
 */
@JvmInline
public value class AlignContent private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "SpaceBetween"
    4 -> "SpaceAround"
    5 -> "Stretch"
    else -> "Unknown: $ordinal"
  }

  public companion object {
    public val FlexStart: AlignContent = AlignContent(0)
    public val FlexEnd: AlignContent = AlignContent(1)
    public val Center: AlignContent = AlignContent(2)
    public val SpaceBetween: AlignContent = AlignContent(3)
    public val SpaceAround: AlignContent = AlignContent(4)
    public val Stretch: AlignContent = AlignContent(5)

    public fun valueOf(ordinal: Int): AlignContent {
      require(ordinal in 0..5) { "unknown AlignContent: $ordinal" }
      return AlignContent(ordinal)
    }
  }
}

/**
 * This attribute controls the alignment along the cross axis.
 */
@JvmInline
public value class AlignItems private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "Baseline"
    4 -> "Stretch"
    else -> "Unknown: $ordinal"
  }

  public companion object {
    public val FlexStart: AlignItems = AlignItems(0)
    public val FlexEnd: AlignItems = AlignItems(1)
    public val Center: AlignItems = AlignItems(2)
    public val Baseline: AlignItems = AlignItems(3)
    public val Stretch: AlignItems = AlignItems(4)

    public fun valueOf(ordinal: Int): AlignItems {
      require(ordinal in 0..4) { "unknown AlignItems: $ordinal" }
      return AlignItems(ordinal)
    }
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
public value class AlignSelf private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "Baseline"
    4 -> "Stretch"
    5 -> "Auto"
    else -> "Unknown: $ordinal"
  }

  public companion object {
    public val FlexStart: AlignSelf = AlignSelf(0)
    public val FlexEnd: AlignSelf = AlignSelf(1)
    public val Center: AlignSelf = AlignSelf(2)
    public val Baseline: AlignSelf = AlignSelf(3)
    public val Stretch: AlignSelf = AlignSelf(4)
    public val Auto: AlignSelf = AlignSelf(5)

    public fun valueOf(ordinal: Int): AlignSelf {
      require(ordinal in 0..5) { "unknown AlignSelf: $ordinal" }
      return AlignSelf(ordinal)
    }
  }
}

/**
 * The direction children items are placed inside the flex container, it determines the
 * direction of the main axis (and the cross axis, perpendicular to the main axis).
 */
@JvmInline
public value class FlexDirection private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Row"
    1 -> "RowReverse"
    2 -> "Column"
    3 -> "ColumnReverse"
    else -> "Unknown: $ordinal"
  }

  public companion object {
    public val Row: FlexDirection = FlexDirection(0)
    public val RowReverse: FlexDirection = FlexDirection(1)
    public val Column: FlexDirection = FlexDirection(2)
    public val ColumnReverse: FlexDirection = FlexDirection(3)

    public fun valueOf(ordinal: Int): FlexDirection {
      require(ordinal in 0..3) { "unknown FlexDirection: $ordinal" }
      return FlexDirection(ordinal)
    }
  }
}

/**
 * This attribute controls whether the flex container is single-line or multi-line,
 * and the direction of the cross axis.
 */
@JvmInline
public value class FlexWrap private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "NoWrap"
    1 -> "Wrap"
    2 -> "WrapReverse"
    else -> "Unknown: $ordinal"
  }

  public companion object {
    public val NoWrap: FlexWrap = FlexWrap(0)
    public val Wrap: FlexWrap = FlexWrap(1)
    public val WrapReverse: FlexWrap = FlexWrap(2)

    public fun valueOf(ordinal: Int): FlexWrap {
      require(ordinal in 0..2) { "unknown FlexWrap: $ordinal" }
      return FlexWrap(ordinal)
    }
  }
}

/**
 * This attribute controls the alignment along the main axis.
 */
@JvmInline
public value class JustifyContent private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "FlexStart"
    1 -> "FlexEnd"
    2 -> "Center"
    3 -> "SpaceBetween"
    4 -> "SpaceAround"
    5 -> "SpaceEvenly"
    else -> "Unknown: $ordinal"
  }

  public companion object {
    public val FlexStart: JustifyContent = JustifyContent(0)
    public val FlexEnd: JustifyContent = JustifyContent(1)
    public val Center: JustifyContent = JustifyContent(2)
    public val SpaceBetween: JustifyContent = JustifyContent(3)
    public val SpaceAround: JustifyContent = JustifyContent(4)
    public val SpaceEvenly: JustifyContent = JustifyContent(5)

    public fun valueOf(ordinal: Int): JustifyContent {
      require(ordinal in 0..5) { "unknown JustifyContent: $ordinal" }
      return JustifyContent(ordinal)
    }
  }
}

/**
 * A MeasureSpec encapsulates the layout requirements passed from parent to child.
 * Each MeasureSpec represents a requirement for either the width or the height.
 * A MeasureSpec is comprised of a size and a mode.
 */
@JvmInline
public value class MeasureSpec internal constructor(internal val value: Int) {

  public val size: Int get() = value and 0x3FFF
  public val mode: MeasureSpecMode get() = MeasureSpecMode.valueOf(abs(value shr 30))

  public companion object {
    public const val MaxSize: Int = Int.MAX_VALUE and 0x00FFFFFF

    public fun from(size: Int, mode: MeasureSpecMode): MeasureSpec {
      require(size in 0..MaxSize) { "invalid size: $size" }
      // Use the top 2 bits for the mode and use the bottom 30 bits for the size.
      return MeasureSpec((mode.ordinal shl 30) or (size and 0x3FFF))
    }
  }
}

/**
 * A [MeasureSpec] mode.
 */
@JvmInline
public value class MeasureSpecMode private constructor(public val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Unspecified"
    1 -> "Exactly"
    2 -> "AtMost"
    else -> "Unknown: $ordinal"
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

    public fun valueOf(ordinal: Int): MeasureSpecMode {
      require(ordinal in 0..2) { "unknown MeasureSpecMode: $ordinal" }
      return MeasureSpecMode(ordinal)
    }
  }
}

/**
 * A generic spacing container that can be used for padding or margin.
 */
public data class Spacing(
  val start: Int = 0,
  val end: Int = 0,
  val top: Int = 0,
  val bottom: Int = 0,
) {
  public companion object {
    public val Zero: Spacing = Spacing()
  }
}

/**
 * Create a new [Size] instance.
 */
public fun Size(width: Int, height: Int): Size {
  require(width >= 0 && height >= 0) { "invalid size: [$width, $height]" }
  return Size(packLong(width, height))
}

/**
 * A two-dimensional size composed of two [Int]s.
 */
@JvmInline
public value class Size internal constructor(private val value: Long) {
  public val width: Int get() = unpackLower(value)
  public val height: Int get() = unpackHigher(value)
}
