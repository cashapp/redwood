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
package example.schema

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Schema
import app.cash.redwood.schema.Widget

@Schema(
  [
    Row::class,
    Column::class,
    ScrollableColumn::class,
    TextInput::class,
    Text::class,
    Image::class,
  ],
)
interface EmojiSearch

@Widget(1)
data class Row(
  @Children(1) val children: () -> Unit,
)

@Widget(2)
data class Column(
  @Children(1) val children: () -> Unit,
)

@Widget(3)
data class ScrollableColumn(
  @Children(1) val children: () -> Unit,
)

@Widget(4)
data class TextInput(
  @Property(1) val hint: String,
  @Property(2) val text: String,
  @Property(3) val onTextChanged: (String) -> Unit,
)

@Widget(5)
data class Text(
  @Property(1) val text: String,
)

@Widget(6)
data class Image(
  @Property(1) val url: String,
)
