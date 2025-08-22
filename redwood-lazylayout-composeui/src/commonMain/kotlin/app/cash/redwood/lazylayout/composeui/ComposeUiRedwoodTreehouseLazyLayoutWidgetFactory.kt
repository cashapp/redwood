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
package app.cash.redwood.lazylayout.composeui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.toPlatformDp
import app.cash.redwood.widget.compose.ComposeWidgetChildren

public class ComposeUiRedwoodLazyLayoutWidgetFactory : AbstractComposeUiRedwoodLazyLayoutWidgetFactory() {
  @Composable
  override fun LazyListBinding(
    isVertical: Boolean,
    onViewportChanged: (Int, Int) -> Unit,
    itemsBefore: Int,
    itemsAfter: Int,
    width: Constraint,
    height: Constraint,
    margin: Margin,
    crossAxisAlignment: CrossAxisAlignment,
    scrollItemIndex: ScrollItemIndex,
    placeholder: ComposeWidgetChildren,
    items: ComposeWidgetChildren,
    modifier: Modifier,
  ) {
    LazyList(
      isVertical = isVertical,
      onViewportChanged = onViewportChanged,
      itemsBefore = itemsBefore,
      itemsAfter = itemsAfter,
      refreshing = false,
      onRefresh = null,
      width = width,
      height = height,
      margin = margin,
      crossAxisAlignment = crossAxisAlignment,
      scrollItemIndex = scrollItemIndex,
      pullRefreshContentColor = 0U,
      placeholder = placeholder,
      items = items,
      modifier = modifier,
    )
  }

  @Composable
  override fun RefreshableLazyListBinding(
    isVertical: Boolean,
    onViewportChanged: (Int, Int) -> Unit,
    itemsBefore: Int,
    itemsAfter: Int,
    refreshing: Boolean,
    onRefresh: (() -> Unit)?,
    width: Constraint,
    height: Constraint,
    margin: Margin,
    crossAxisAlignment: CrossAxisAlignment,
    scrollItemIndex: ScrollItemIndex,
    pullRefreshContentColor: UInt,
    placeholder: ComposeWidgetChildren,
    items: ComposeWidgetChildren,
    modifier: Modifier,
  ) {
    LazyList(
      isVertical = isVertical,
      onViewportChanged = onViewportChanged,
      itemsBefore = itemsBefore,
      itemsAfter = itemsAfter,
      refreshing = refreshing,
      onRefresh = onRefresh,
      width = width,
      height = height,
      margin = margin,
      crossAxisAlignment = crossAxisAlignment,
      scrollItemIndex = scrollItemIndex,
      pullRefreshContentColor = pullRefreshContentColor,
      placeholder = placeholder,
      items = items,
      modifier = modifier,
    )
  }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun LazyList(
  isVertical: Boolean,
  onViewportChanged: (Int, Int) -> Unit,
  itemsBefore: Int,
  itemsAfter: Int,
  refreshing: Boolean,
  onRefresh: (() -> Unit)?,
  width: Constraint,
  height: Constraint,
  margin: Margin,
  crossAxisAlignment: CrossAxisAlignment,
  scrollItemIndex: ScrollItemIndex,
  pullRefreshContentColor: UInt,
  placeholder: ComposeWidgetChildren,
  items: ComposeWidgetChildren,
  modifier: Modifier,
) {
  val content: LazyListScope.() -> Unit = {
    items(items.widgets) { item ->
      val modifier = if (crossAxisAlignment != CrossAxisAlignment.Stretch) {
        Modifier
      } else if (isVertical) {
        Modifier.fillMaxWidth()
      } else {
        Modifier.fillMaxHeight()
      }
      item.value.invoke(modifier)
    }
  }
  Box(modifier) {
    var isRefreshing by remember(refreshing) { mutableStateOf(refreshing) }
    val refreshState = rememberPullRefreshState(
      refreshing = isRefreshing,
      onRefresh = {
        // This looks strange, but the other platforms all assume that `refreshing = true` after
        // onRefresh is called. To maintain consistency we do the same, otherwise the refresh
        // indicator disappears whilst we wait for the presenter to send `refreshing = true`
        isRefreshing = true
        onRefresh?.invoke()
      },
    )
    PullRefreshIndicator(
      refreshing = isRefreshing,
      state = refreshState,
      // Should this be placed somewhere different when horizontal
      modifier = Modifier.align(Alignment.TopCenter),
      contentColor = Color(pullRefreshContentColor.toLong()),
    )

    // TODO Fix item count truncation
    val state = rememberLazyListState()
    val lastVisibleItemIndex by remember {
      derivedStateOf { state.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
    }
    LaunchedEffect(lastVisibleItemIndex) {
      lastVisibleItemIndex?.let { lastVisibleItemIndex ->
        onViewportChanged(state.firstVisibleItemIndex, lastVisibleItemIndex)
      }
    }
    LaunchedEffect(scrollItemIndex) {
      state.scrollToItem(scrollItemIndex.index)
    }

    val modifier = Modifier
      .run { if (width == Constraint.Fill) fillMaxWidth() else this }
      .run { if (height == Constraint.Fill) fillMaxHeight() else this }
      .padding(
        start = margin.start.toPlatformDp().dp,
        top = margin.top.toPlatformDp().dp,
        end = margin.end.toPlatformDp().dp,
        bottom = margin.bottom.toPlatformDp().dp,
      )
      .pullRefresh(state = refreshState, enabled = onRefresh != null)
    if (isVertical) {
      val horizontalAlignment = when (crossAxisAlignment) {
        CrossAxisAlignment.Start -> Alignment.Start
        CrossAxisAlignment.Center -> Alignment.CenterHorizontally
        CrossAxisAlignment.End -> Alignment.End
        CrossAxisAlignment.Stretch -> Alignment.Start
        else -> throw AssertionError()
      }
      LazyColumn(
        modifier = modifier,
        state = state,
        horizontalAlignment = horizontalAlignment,
        content = content,
      )
    } else {
      LazyRow(
        modifier = modifier,
        state = state,
        verticalAlignment = when (crossAxisAlignment) {
          CrossAxisAlignment.Start -> Alignment.Top
          CrossAxisAlignment.Center -> Alignment.CenterVertically
          CrossAxisAlignment.End -> Alignment.Bottom
          CrossAxisAlignment.Stretch -> Alignment.Top
          else -> throw AssertionError()
        },
        content = content,
      )
    }
  }
}
