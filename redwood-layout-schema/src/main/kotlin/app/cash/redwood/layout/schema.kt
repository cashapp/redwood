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

import app.cash.redwood.schema.Schema

@Schema(
  [
    // Widgets
    Box::class,
    Column::class,
    Row::class,
    Spacer::class,
    // Next tag: 5

    // Modifiers
    Flex::class,
    Grow::class,
    Height::class,
    HorizontalAlignment::class,
    Margin::class,
    MatchParentSize::class,
    Shrink::class,
    Size::class,
    VerticalAlignment::class,
    Width::class,
    // Next tag: 11
  ],
)
public interface RedwoodLayout
