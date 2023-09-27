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
package app.cash.redwood.layout

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.api.MainAxisAlignment
import app.cash.redwood.layout.api.Overflow
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Default
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Widget
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin

/**
 * Lays out widgets horizontally in a row.
 */
@Widget(1)
public data class Row(
  /**
   * Sets whether the row's width will wrap its contents ([Constraint.Wrap]) or match the width of
   * its parent ([Constraint.Fill]).
   */
  @Property(1)
  @Default("Constraint.Wrap")
  val width: Constraint,

  /**
   * Sets whether the row's height will wrap its contents ([Constraint.Wrap]) or match the height of
   * its parent ([Constraint.Fill]).
   */
  @Property(2)
  @Default("Constraint.Wrap")
  val height: Constraint,

  /**
   * Applies margin (space) around the row.
   *
   * This can also be applied to an individual widget using `Modifier.margin`.
   */
  @Property(3)
  @Default("Margin.Zero")
  val margin: Margin,

  /**
   * Sets whether the row allows scrolling ([Overflow.Scroll]) if its content overflows its bounds
   * or if it does not allow scrolling ([Overflow.Clip]).
   */
  @Property(4)
  @Default("Overflow.Clip")
  val overflow: Overflow,

  /**
   * Sets the horizontal alignment for widgets in this row.
   */
  @Property(5)
  @Default("MainAxisAlignment.Start")
  val horizontalAlignment: MainAxisAlignment,

  /**
   * Sets the default vertical alignment for widgets in this row.
   *
   * This can also be applied to an individual widget using `Modifier.verticalAlignment`.
   */
  @Property(6)
  @Default("CrossAxisAlignment.Start")
  val verticalAlignment: CrossAxisAlignment,

  /**
   * A slot to add widgets in.
   */
  @Children(1) val children: RowScope.() -> Unit,
)

public object RowScope

/**
 * Lays out widgets vertically in a column.
 */
@Widget(2)
public data class Column(
  /**
   * Sets whether the column's width will wrap its contents ([Constraint.Wrap]) or match the width
   * of its parent ([Constraint.Fill]).
   */
  @Property(1)
  @Default("Constraint.Wrap")
  val width: Constraint,

  /**
   * Sets whether the column's height will wrap its contents ([Constraint.Wrap]) or match the height
   * of its parent ([Constraint.Fill]).
   */
  @Property(2)
  @Default("Constraint.Wrap")
  val height: Constraint,

  /**
   * Applies margin (space) around the column.
   *
   * This can also be applied to an individual widget using `Modifier.margin`.
   */
  @Property(3)
  @Default("Margin.Zero")
  val margin: Margin,

  /**
   * Sets whether the column allows scrolling ([Overflow.Scroll]) if its content overflows its bounds
   * or if it does not allow scrolling ([Overflow.Clip]).
   */
  @Property(4)
  @Default("Overflow.Clip")
  val overflow: Overflow,

  /**
   * Sets the default horizontal alignment for widgets in this column.
   *
   * This can also be applied to an individual widget using `Modifier.horizontalAlignment`.
   */
  @Property(5)
  @Default("CrossAxisAlignment.Start")
  val horizontalAlignment: CrossAxisAlignment,

  /**
   * Sets the vertical alignment for widgets in this column.
   */
  @Property(6)
  @Default("MainAxisAlignment.Start")
  val verticalAlignment: MainAxisAlignment,

  /**
   * A slot to add widgets in.
   */
  @Children(1) val children: ColumnScope.() -> Unit,
)

public object ColumnScope

/**
 * Adds empty space between other widgets.
 */
@Widget(3)
public data class Spacer(
  /**
   * Sets the width of the spacer.
   */
  @Property(1)
  @Default("Dp(0.0)")
  val width: Dp,

  /**
   * Sets the height of the spacer.
   */
  @Property(2)
  @Default("Dp(0.0)")
  val height: Dp,
)

/**
 * Lays out widgets along the z-axis in a column.
 *
 * Minimum and maximum heights do not include margins.
 */
@Widget(4)
public data class Box(
  /**
   * Sets whether the box's width will match its widest child ([Constraint.Wrap]) or match the width
   * of its parent ([Constraint.Fill]).
   */
  @Property(1)
  @Default("Constraint.Wrap")
  val width: Constraint,

  /**
   * Sets whether the box's height will match its tallest child ([Constraint.Wrap]) or match the
   * height of its parent ([Constraint.Fill]).
   */
  @Property(2)
  @Default("Constraint.Wrap")
  val height: Constraint,

  /**
   * Applies margin (space) around the box.
   *
   * This can also be applied to an individual widget using `Modifier.margin`.
   */
  @Property(3)
  @Default("Margin.Zero")
  val margin: Margin,

  /**
   * Sets the default horizontal alignment for widgets in this Box.
   *
   * This can also be applied to an individual widget using `Modifier.horizontalAlignment`.
   */
  @Property(4)
  @Default("CrossAxisAlignment.Start")
  val horizontalAlignment: CrossAxisAlignment,

  /**
   * Sets the default vertical alignment for widgets in this Box.
   *
   * This can also be applied to an individual widget using `Modifier.horizontalAlignment`.
   */
  @Property(5)
  @Default("CrossAxisAlignment.Start")
  val verticalAlignment: CrossAxisAlignment,

  /**
   * A slot to add widgets in.
   */
  @Children(1) val children: BoxScope.() -> Unit,
)

public object BoxScope
