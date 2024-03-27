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
package com.example.redwood.testing

import app.cash.redwood.layout.RedwoodLayout
import app.cash.redwood.lazylayout.RedwoodLazyLayout
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Default
import app.cash.redwood.schema.Modifier
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Schema.Dependency
import app.cash.redwood.schema.Widget
import kotlin.time.Duration

@Schema(
  members = [
    TestRow::class,
    ScopedTestRow::class,
    TestRowVerticalAlignment::class,
    AccessibilityDescription::class,
    CustomType::class,
    CustomTypeStateless::class,
    CustomTypeWithDefault::class,
    CustomTypeWithMultipleScopes::class,
    CustomTypeDataObject::class,
    Text::class,
    Button::class,
    Button2::class,
    TextInput::class,
    Rectangle::class,
    Split::class,
  ],
  dependencies = [
    Dependency(1, RedwoodLayout::class),
    Dependency(2, RedwoodLazyLayout::class),
  ],
)
public interface TestSchema

/**
 * A trivial row-like type for testing purposes only.
 * Use redwood-layout for any real views in the test app.
 */
@Widget(1)
public data class TestRow(
  @Children(1) val children: () -> Unit,
)

public object TestRowScope

/**
 * A trivial row-like type with a scope for testing purposes only.
 * Use redwood-layout for any real views in the test app.
 */
@Widget(2)
public data class ScopedTestRow(
  @Children(1) val children: TestRowScope.() -> Unit,
)

@Widget(3)
public data class Text(
  @Property(1) val text: String?,
)

@Widget(4)
public data class Button(
  @Property(1) val text: String?,
  @Property(2) val onClick: (() -> Unit)?,
)

/** Like [Button] but with a required lambda. */
@Widget(7)
public data class Button2(
  @Property(1) val text: String?,
  @Property(2) val onClick: () -> Unit,
)

@Widget(5)
public data class TextInput(
  @Property(1) val text: String?,
  @Property(2) val customType: Duration?,
  @Property(3) val onChange: (String) -> Unit,
  @Property(4) val onChangeCustomType: (Duration) -> Unit,
  @Property(5) val maxLength: Int,
)

@Widget(8)
public data class Rectangle(
  /** expects argb format: 0xAARRGGBBu*/
  @Property(1) val backgroundColor: UInt,

  @Default("0f")
  @Property(2) val cornerRadius: Float,
)

@Widget(9)
public data class Split(
  @Children(1) val left: () -> Unit,
  @Children(2) val right: () -> Unit,
)

public object TestScope

public object SecondaryTestScope

@Modifier(1, TestRowScope::class)
public data class TestRowVerticalAlignment(
  /** -1 for top, 0 for middle, 1 for bottom. */
  val direction: Int,
)

@Modifier(2, TestScope::class)
public data class AccessibilityDescription(
  val value: String,
)

@Modifier(3, TestScope::class)
public data class CustomType(
  val customType: Duration,
)

@Modifier(4, TestScope::class)
public object CustomTypeStateless

@Modifier(5, TestScope::class)
public data class CustomTypeWithDefault(
  val customType: Duration,
  @Default("\"sup\"") val string: String,
)

@Modifier(6, TestScope::class, SecondaryTestScope::class)
public object CustomTypeWithMultipleScopes

@Modifier(7, TestScope::class)
public data object CustomTypeDataObject
