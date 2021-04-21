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
package example.sunspot

import app.cash.treehouse.schema.Children
import app.cash.treehouse.schema.Default
import app.cash.treehouse.schema.Property
import app.cash.treehouse.schema.Schema
import app.cash.treehouse.schema.Widget

@Schema(
  [
    SunspotBox::class,
    SunspotText::class,
    SunspotButton::class,
  ]
)
interface Sunspot

@Widget(1)
data class SunspotBox(
  @Children(1) val children: List<Any>,
)

@Widget(2)
data class SunspotText(
  @Property(1) val text: String?,
  @Property(2) @Default("\"black\"") val color: String,
)

@Widget(3)
data class SunspotButton(
  @Property(1) val text: String?,
  @Property(2) @Default("true") val enabled: Boolean,
  @Property(3) val onClick: () -> Unit,
)
