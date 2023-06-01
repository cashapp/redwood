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
package app.cash.redwood.lazylayout

import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Widget

@Widget(1)
public data class LazyList(
  @Property(1) val isVertical: Boolean,
  @Property(2) val onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit,
  @Property(3) val itemsBefore: Int,
  @Property(4) val itemsAfter: Int,
  @Children(1) val placeholder: () -> Unit,
  @Children(2) val items: () -> Unit,
)

@Widget(2)
public data class RefreshableLazyList(
  @Property(1) val isVertical: Boolean,
  @Property(2) val onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit,
  @Property(3) val itemsBefore: Int,
  @Property(4) val itemsAfter: Int,
  @Property(5) val refreshing: Boolean,
  @Property(6) val onRefresh: (() -> Unit)?,
  @Children(1) val placeholder: () -> Unit,
  @Children(2) val items: () -> Unit,
)
