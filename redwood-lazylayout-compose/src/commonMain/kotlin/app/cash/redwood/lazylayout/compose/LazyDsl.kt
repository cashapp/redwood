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
package app.cash.redwood.lazylayout.compose

import androidx.compose.runtime.Composable
import app.cash.redwood.LayoutScopeMarker
import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.ui.Margin

/**
 * Receiver scope which is used by [LazyColumn] and [LazyRow].
 */
@LayoutScopeMarker
public interface LazyListScope {
  /**
   * Adds a single item.
   *
   * @param content The content of the item.
   */
  public fun item(
    content: @Composable () -> Unit,
  )

  /**
   * Adds a [count] of items.
   *
   * @param count The items count.
   * @param itemContent The content displayed by a single item.
   */
  public fun items(
    count: Int,
    itemContent: @Composable (index: Int) -> Unit,
  )
}

/**
 * Adds a list of items.
 *
 * @param items The data list.
 * @param itemContent The content displayed by a single item.
 */
public inline fun <T> LazyListScope.items(
  items: List<T>,
  crossinline itemContent: @Composable (item: T) -> Unit,
): Unit = items(items.size) {
  itemContent(items[it])
}

/**
 * Adds a list of items where the content of an item is aware of its index.
 *
 * @param items The data list.
 * @param itemContent The content displayed by a single item.
 */
public inline fun <T> LazyListScope.itemsIndexed(
  items: List<T>,
  crossinline itemContent: @Composable (index: Int, item: T) -> Unit,
): Unit = items(
  items.size,
) {
  itemContent(it, items[it])
}

/**
 * Adds an array of items.
 *
 * @param items The data array.
 * @param itemContent The content displayed by a single item.
 */
public inline fun <T> LazyListScope.items(
  items: Array<T>,
  crossinline itemContent: @Composable (item: T) -> Unit,
): Unit = items(
  items.size,
) {
  itemContent(items[it])
}

/**
 * Adds an array of items where the content of an item is aware of its index.
 *
 * @param items The data array.
 * @param itemContent The content displayed by a single item.
 */
public inline fun <T> LazyListScope.itemsIndexed(
  items: Array<T>,
  crossinline itemContent: @Composable (index: Int, item: T) -> Unit,
): Unit = items(
  items.size,
) {
  itemContent(it, items[it])
}

@RequiresOptIn("This Redwood LazyLayout API is experimental and may change in the future.")
public annotation class ExperimentalRedwoodLazyLayoutApi

/**
 * The horizontally scrolling list that only composes and lays out the currently visible items.
 * The [content] block defines a DSL which allows you to emit items of different types. For
 * example you can use [LazyListScope.item] to add a single item and [LazyListScope.items] to add
 * a list of items.
 *
 * The purpose of [placeholder] is to define the temporary content of an on-screen item while the
 * content of that item (as described by the [content] block) is being retrieved. When the content
 * of that item has been retrieved, the [placeholder] is replaced with that of the content.
 *
 * @param state The state object to be used to control or observe the list's state.
 * @param width Sets whether the row's width will wrap its contents ([Constraint.Wrap]) or match the
 * width of its parent ([Constraint.Fill]).
 * @param height Sets whether the row's height will wrap its contents ([Constraint.Wrap]) or match
 * the height of its parent ([Constraint.Fill]).
 * @param margin Applies margin (space) around the list. This can also be applied to an individual
 * item using `Modifier.margin`.
 * @param verticalAlignment the vertical alignment applied to the items.
 * @param modifier The modifier to apply to this layout.
 * @param placeholder A block which describes the content of each placeholder item. Note that the
 * placeholder block will be invoked multiple times, and assumes that the content and its sizing on
 * each invocation is identical to one another.
 * @param content A block which describes the content. Inside this block you can use methods like
 * [LazyListScope.item] to add a single item or [LazyListScope.items] to add a list of items.
 */
@Composable
public fun LazyRow(
  state: LazyListState = rememberLazyListState(),
  width: Constraint = Constraint.Wrap,
  height: Constraint = Constraint.Wrap,
  margin: Margin = Margin.Zero,
  verticalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
  modifier: Modifier = Modifier,
  placeholder: @Composable () -> Unit,
  content: LazyListScope.() -> Unit,
) {
  LazyList(
    isVertical = false,
    state = state,
    width = width,
    height = height,
    margin = margin,
    crossAxisAlignment = verticalAlignment,
    modifier = modifier,
    placeholder = placeholder,
    content = content,
  )
}

/**
 * The horizontally scrolling list that only composes and lays out the currently visible items.
 * The [content] block defines a DSL which allows you to emit items of different types. For
 * example you can use [LazyListScope.item] to add a single item and [LazyListScope.items] to add
 * a list of items.
 *
 * This function differs from the other [LazyRow] function, in that a refresh indicator is
 * conditionally displayed via a vertical swipe gesture when at the beginning of the list. The
 * appropriate response to this gesture can be supplied via the [onRefresh] callback.
 *
 * The purpose of [placeholder] is to define the temporary content of an on-screen item while the
 * content of that item (as described by the [content] block) is being retrieved. When the content
 * of that item has been retrieved, the [placeholder] is replaced with that of the content.
 *
 * @param refreshing Whether or not the list should show the pull-to-refresh indicator.
 * @param onRefresh Called when a swipe gesture triggers a pull-to-refresh.
 * @param state The state object to be used to control or observe the list's state.
 * @param width Sets whether the row's width will wrap its contents ([Constraint.Wrap]) or match the
 * width of its parent ([Constraint.Fill]).
 * @param height Sets whether the row's height will wrap its contents ([Constraint.Wrap]) or match
 * the height of its parent ([Constraint.Fill]).
 * @param margin Applies margin (space) around the list. This can also be applied to an individual
 * item using `Modifier.margin`.
 * @param verticalAlignment the vertical alignment applied to the items.
 * @param pullRefreshContentColor The color of the pull-to-refresh indicator as an ARGB color int.
 * For example, the color `0xFF00AA00` would be a dark green with 100% opacity.
 * @param modifier The modifier to apply to this layout.
 * @param placeholder A block which describes the content of each placeholder item. Note that the
 * placeholder block will be invoked multiple times, and assumes that the content and its sizing on
 * each invocation is identical to one another.
 * @param content A block which describes the content. Inside this block you can use methods like
 * [LazyListScope.item] to add a single item or [LazyListScope.items] to add a list of items.
 */
@ExperimentalRedwoodLazyLayoutApi
@Composable
public fun LazyRow(
  refreshing: Boolean,
  onRefresh: (() -> Unit)?,
  state: LazyListState = rememberLazyListState(),
  width: Constraint = Constraint.Wrap,
  height: Constraint = Constraint.Wrap,
  margin: Margin = Margin.Zero,
  verticalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
  pullRefreshContentColor: UInt = 0xFF000000u,
  modifier: Modifier = Modifier,
  placeholder: @Composable () -> Unit,
  content: LazyListScope.() -> Unit,
) {
  RefreshableLazyList(
    isVertical = false,
    refreshing = refreshing,
    onRefresh = onRefresh,
    state = state,
    width = width,
    height = height,
    margin = margin,
    crossAxisAlignment = verticalAlignment,
    pullRefreshContentColor = pullRefreshContentColor,
    modifier = modifier,
    placeholder = placeholder,
    content = content,
  )
}

/**
 * The vertically scrolling list that only composes and lays out the currently visible items.
 * The [content] block defines a DSL which allows you to emit items of different types. For
 * example you can use [LazyListScope.item] to add a single item and [LazyListScope.items] to add
 * a list of items.
 *
 * The purpose of [placeholder] is to define the temporary content of an on-screen item while the
 * content of that item (as described by the [content] block) is being retrieved. When the content
 * of that item has been retrieved, the [placeholder] is replaced with that of the content.
 *
 * @param state The state object to be used to control or observe the list's state.
 * @param width Sets whether the row's width will wrap its contents ([Constraint.Wrap]) or match the
 * width of its parent ([Constraint.Fill]).
 * @param height Sets whether the row's height will wrap its contents ([Constraint.Wrap]) or match
 * the height of its parent ([Constraint.Fill]).
 * @param margin Applies margin (space) around the list. This can also be applied to an individual
 * item using `Modifier.margin`.
 * @param horizontalAlignment The horizontal alignment applied to the items.
 * @param modifier The modifier to apply to this layout.
 * @param placeholder A block which describes the content of each placeholder item. Note that the
 * placeholder block will be invoked multiple times, and assumes that the content and its sizing on
 * each invocation is identical to one another.
 * @param content A block which describes the content. Inside this block you can use methods like
 * [LazyListScope.item] to add a single item or [LazyListScope.items] to add a list of items.
 */
@Composable
public fun LazyColumn(
  state: LazyListState = rememberLazyListState(),
  width: Constraint = Constraint.Wrap,
  height: Constraint = Constraint.Wrap,
  margin: Margin = Margin.Zero,
  horizontalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
  modifier: Modifier = Modifier,
  placeholder: @Composable () -> Unit,
  content: LazyListScope.() -> Unit,
) {
  LazyList(
    isVertical = true,
    state = state,
    width = width,
    height = height,
    margin = margin,
    crossAxisAlignment = horizontalAlignment,
    modifier = modifier,
    placeholder = placeholder,
    content = content,
  )
}

/**
 * The vertically scrolling list that only composes and lays out the currently visible items.
 * The [content] block defines a DSL which allows you to emit items of different types. For
 * example you can use [LazyListScope.item] to add a single item and [LazyListScope.items] to add
 * a list of items.
 *
 * This function differs from the other [LazyColumn] function, in that a refresh indicator is
 * conditionally displayed via a vertical swipe gesture when at the beginning of the list. The
 * appropriate response to this gesture can be supplied via the [onRefresh] callback.
 *
 * The purpose of [placeholder] is to define the temporary content of an on-screen item while the
 * content of that item (as described by the [content] block) is being retrieved. When the content
 * of that item has been retrieved, the [placeholder] is replaced with that of the content.
 *
 * @param refreshing Whether or not the list should show the pull-to-refresh indicator.
 * @param onRefresh Called when a swipe gesture triggers a pull-to-refresh.
 * @param state The state object to be used to control or observe the list's state.
 * @param width Sets whether the row's width will wrap its contents ([Constraint.Wrap]) or match the
 * width of its parent ([Constraint.Fill]).
 * @param height Sets whether the row's height will wrap its contents ([Constraint.Wrap]) or match
 * the height of its parent ([Constraint.Fill]).
 * @param margin Applies margin (space) around the list. This can also be applied to an individual
 * item using `Modifier.margin`.
 * @param horizontalAlignment The horizontal alignment applied to the items.
 * @param pullRefreshContentColor The color of the pull-to-refresh indicator as an ARGB color int.
 * For example, the color `0xFF00AA00` would be a dark green with 100% opacity.
 * @param modifier The modifier to apply to this layout.
 * @param placeholder A block which describes the content of each placeholder item. Note that the
 * placeholder block will be invoked multiple times, and assumes that the content and its sizing on
 * each invocation is identical to one another.
 * @param content A block which describes the content. Inside this block you can use methods like
 * [LazyListScope.item] to add a single item or [LazyListScope.items] to add a list of items.
 */
@ExperimentalRedwoodLazyLayoutApi
@Composable
public fun LazyColumn(
  refreshing: Boolean,
  onRefresh: (() -> Unit)?,
  state: LazyListState = rememberLazyListState(),
  width: Constraint = Constraint.Wrap,
  height: Constraint = Constraint.Wrap,
  margin: Margin = Margin.Zero,
  horizontalAlignment: CrossAxisAlignment = CrossAxisAlignment.Start,
  pullRefreshContentColor: UInt = 0xFF000000u,
  modifier: Modifier = Modifier,
  placeholder: @Composable () -> Unit,
  content: LazyListScope.() -> Unit,
) {
  RefreshableLazyList(
    isVertical = true,
    refreshing = refreshing,
    onRefresh = onRefresh,
    state = state,
    width = width,
    height = height,
    margin = margin,
    crossAxisAlignment = horizontalAlignment,
    pullRefreshContentColor = pullRefreshContentColor,
    modifier = modifier,
    placeholder = placeholder,
    content = content,
  )
}
