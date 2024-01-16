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

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.schema.Children
import app.cash.redwood.schema.Property
import app.cash.redwood.schema.Widget
import app.cash.redwood.ui.Margin

/**
 * The documentation for [LazyList] is a subset of the documentation for [RefreshableLazyList]. In
 * order to avoid documentation duplication, see [LazyList]. The documentation should be unified
 * once https://github.com/cashapp/redwood/issues/1084 is implemented and [RefreshableLazyList] is
 * deprecated and removed.
 *
 * @see LazyList
 */
@Widget(1)
public data class LazyList(
  @Property(1) val isVertical: Boolean,
  @Property(2) val onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit,
  @Property(3) val itemsBefore: Int,
  @Property(4) val itemsAfter: Int,
  @Property(5) val width: Constraint,
  @Property(6) val height: Constraint,
  @Property(7) val margin: Margin,
  @Property(8) val crossAxisAlignment: CrossAxisAlignment,
  @Property(9) val scrollItemIndex: ScrollItemIndex,
  @Children(1) val placeholder: () -> Unit,
  @Children(2) val items: () -> Unit,
)

@Widget(2)
public data class RefreshableLazyList(
  /**
   * Whether the list should be vertically oriented.
   */
  @Property(1) val isVertical: Boolean,

  /**
   * Invoked when the user has scrolled the list, such that the `firstVisibleItemIndex` or the
   * `lastVisibleItemIndex` has changed. When the user performs a fling, [onViewportChanged] will be
   * invoked multiple times.
   *
   * The `firstVisibleItemIndex` is the index of the first partially visible item within the
   * viewport. The `lastVisibleItemIndex` is the index of the last partially visible item within the
   * viewport.
   */
  @Property(2) val onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit,

  /**
   * The number of un-emitted items before the [items] window.
   *
   * @see [items]
   */
  @Property(3) val itemsBefore: Int,

  /**
   * The number of un-emitted items after the [items] window.
   *
   * @see [items]
   */
  @Property(4) val itemsAfter: Int,

  /**
   * Whether or not the list should show the pull-to-refresh indicator.
   */
  @Property(5) val refreshing: Boolean,

  /**
   * Called when a swipe gesture triggers a pull-to-refresh.
   */
  @Property(6) val onRefresh: (() -> Unit)?,

  /**
   * Sets whether the list's width will wrap its contents ([Constraint.Wrap]) or match the width of
   * its parent ([Constraint.Fill]).
   */
  @Property(7) val width: Constraint,

  /**
   * Sets whether the list's height will wrap its contents ([Constraint.Wrap]) or match the height
   * of its parent ([Constraint.Fill]).
   */
  @Property(8) val height: Constraint,

  /**
   * Applies margin (space) around the list.
   */
  @Property(9) val margin: Margin,

  /**
   * If [isVertical], sets the default horizontal alignment for items in this list. Else, sets the
   * default vertical alignment for items in this list.
   */
  @Property(10) val crossAxisAlignment: CrossAxisAlignment,

  /**
   * The last [ScrollItemIndex] programmatically requested by the user.
   */
  @Property(11) val scrollItemIndex: ScrollItemIndex,

  /**
   * The color of the pull-to-refresh indicator as an ARGB color int.
   */
  @Property(12) val pullRefreshContentColor: UInt,

  /**
   * A block which describes the content of each placeholder item.
   */
  @Children(1) val placeholder: () -> Unit,

  /**
   * The window of items to be inflated by the native lazy list widget implementation. The window
   * should be offset by [itemsAfter], and should have a size of
   * `count - [itemsBefore] - [itemsAfter]`, where `count` is the total number of items that
   * theoretically exists in the list.
   *
   * This field should not be confused with `LazyListScope.items` (et al.) The functions in
   * `LazyListScope` specify what the list theoretically consists of. This property specifies what
   * the list practically consists of, as a function of the current view port. This difference is
   * what distinguishes the `LazyRow` and `LazyColumn` widgets from their non-lazy counterparts
   * (`Row` and `Column`).
   */
  @Children(2) val items: () -> Unit,
)
