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

@Widget(1)
public data class Row(
  @Property(1)
  @Default("Constraint.Wrap")
  val width: Constraint,
  @Property(2)
  @Default("Constraint.Wrap")
  val height: Constraint,
  @Property(3)
  @Default("Margin.Zero")
  val margin: Margin,
  @Property(4)
  @Default("Overflow.Clip")
  val overflow: Overflow,
  @Property(5)
  @Default("MainAxisAlignment.Start")
  val horizontalAlignment: MainAxisAlignment,
  @Property(6)
  @Default("CrossAxisAlignment.Start")
  val verticalAlignment: CrossAxisAlignment,
  @Children(1) val children: RowScope.() -> Unit,
)

public object RowScope

@Widget(2)
public data class Column(
  @Property(1)
  @Default("Constraint.Wrap")
  val width: Constraint,
  @Property(2)
  @Default("Constraint.Wrap")
  val height: Constraint,
  @Property(3)
  @Default("Margin.Zero")
  val margin: Margin,
  @Property(4)
  @Default("Overflow.Clip")
  val overflow: Overflow,
  @Property(5)
  @Default("CrossAxisAlignment.Start")
  val horizontalAlignment: CrossAxisAlignment,
  @Property(6)
  @Default("MainAxisAlignment.Start")
  val verticalAlignment: MainAxisAlignment,
  @Children(1) val children: ColumnScope.() -> Unit,
)

public object ColumnScope

@Widget(3)
public data class Spacer(
  @Property(1)
  @Default("Dp(0.0)")
  val width: Dp,
  @Property(2)
  @Default("Dp(0.0)")
  val height: Dp,
)
