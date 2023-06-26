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
package app.cash.redwood.yoga

import kotlin.jvm.JvmInline

@JvmInline
public value class Direction private constructor(private val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Inherit"
    1 -> "LTR"
    2 -> "RTL"
    else -> throw AssertionError()
  }

  public companion object {
    public val Inherit: Direction = Direction(0)
    public val LTR: Direction = Direction(1)
    public val RTL: Direction = Direction(2)
  }
}

/**
 * The direction children items are placed inside the flex container, it determines the
 * direction of the main axis (and the cross axis, perpendicular to the main axis).
 */
@JvmInline
public value class FlexDirection private constructor(private val ordinal: Int) {

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
 * Returns `true` if this direction's main axis is vertical.
 */
public val FlexDirection.isVertical: Boolean
  get() = this == FlexDirection.Column || this == FlexDirection.ColumnReverse

/**
 * This attribute controls the alignment along the main axis.
 */
@JvmInline
public value class JustifyContent private constructor(private val ordinal: Int) {

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
 * This attribute controls the alignment along the cross axis.
 */
@JvmInline
public value class AlignItems private constructor(private val ordinal: Int) {

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
public value class AlignSelf private constructor(private val ordinal: Int) {

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
