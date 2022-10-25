/*
 * Copyright (C) 2021 Square, Inc.
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
package example.redwood

import app.cash.redwood.layout.RedwoodLayout
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Default
import app.cash.redwood.schema.LayoutModifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Schema.Dependency
import app.cash.redwood.schema.Widget
import kotlin.time.Duration

@Schema(
  members = [
    Row::class,
    ScopedRow::class,
    RowVerticalAlignment::class,
    AccessibilityDescription::class,
    CustomType::class,
    CustomTypeStateless::class,
    CustomTypeWithDefault::class,
    CustomTypeWithMultipleScopes::class,
    Text::class,
    Button::class,
    TextInput::class,
    Space::class,
  ],
  dependencies = [
    Dependency(1, RedwoodLayout::class),
  ],
)
public interface ExampleSchema

@Widget(1)
public data class Row(
  @Children(1) val children: () -> Unit,
)

public object RowScope

@Widget(2)
public data class ScopedRow(
  @Children(1) val children: RowScope.() -> Unit,
)

@Widget(3)
public data class Text(
  @Property(1) val text: String?,
)

@Widget(4)
public data class Button(
  @Property(1) val text: String?,
  @Property(2) val onClick: () -> Unit,
)

@Widget(5)
public data class TextInput(
  @Property(1) val text: String?,
  @Property(2) val customType: Duration?,
  @Property(3) val onChange: (String) -> Unit,
  @Property(4) val onChangeCustomType: (Duration) -> Unit,
)

@Widget(6)
public object Space

public object TestScope

public object SecondaryTestScope

@LayoutModifier(1, RowScope::class)
public data class RowVerticalAlignment(
  /** -1 for top, 0 for middle, 1 for bottom. */
  val direction: Int,
)

@LayoutModifier(2, TestScope::class)
public data class AccessibilityDescription(
  val value: String,
)

@LayoutModifier(3, TestScope::class)
public data class CustomType(
  val customType: Duration,
)

@LayoutModifier(4, TestScope::class)
public object CustomTypeStateless

@LayoutModifier(5, TestScope::class)
public data class CustomTypeWithDefault(
  val customType: Duration,
  @Default("\"sup\"") val string: String,
)

@LayoutModifier(6, TestScope::class, SecondaryTestScope::class)
public object CustomTypeWithMultipleScopes
