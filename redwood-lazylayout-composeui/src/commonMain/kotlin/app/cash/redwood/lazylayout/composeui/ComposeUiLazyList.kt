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
import app.cash.redwood.Modifier as RedwoodModifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.widget.compose.ComposeWidgetChildren

@OptIn(ExperimentalMaterialApi::class)
internal class ComposeUiLazyList :
  LazyList<@Composable () -> Unit>,
  RefreshableLazyList<@Composable () -> Unit> {
  private var isVertical by mutableStateOf(false)
  private var onViewportChanged: ((firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit)? by mutableStateOf(null)
  private var itemsBefore by mutableStateOf(0)
  private var itemsAfter by mutableStateOf(0)
  private var isRefreshing by mutableStateOf(false)
  private var onRefresh: (() -> Unit)? by mutableStateOf(null)
  private var width by mutableStateOf(Constraint.Wrap)
  private var height by mutableStateOf(Constraint.Wrap)

  override var modifier: RedwoodModifier = RedwoodModifier

  override val placeholder = ComposeWidgetChildren()

  override val items = ComposeWidgetChildren()

  override fun isVertical(isVertical: Boolean) {
    this.isVertical = isVertical
  }

  override fun onViewportChanged(onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit) {
    this.onViewportChanged = onViewportChanged
  }

  override fun itemsBefore(itemsBefore: Int) {
    this.itemsBefore = itemsBefore
  }

  override fun itemsAfter(itemsAfter: Int) {
    this.itemsAfter = itemsAfter
  }

  override fun refreshing(refreshing: Boolean) {
    this.isRefreshing = refreshing
  }

  override fun onRefresh(onRefresh: (() -> Unit)?) {
    this.onRefresh = onRefresh
  }

  override fun width(width: Constraint) {
    this.width = width
  }

  override fun height(height: Constraint) {
    this.height = height
  }

  override val value = @Composable {
    val content: LazyListScope.() -> Unit = {
      items(items.widgets) { item ->
        item.value.invoke()
      }
    }
    Box {
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
      )

      // TODO Fix item count truncation
      val state = rememberLazyListState()
      val lastVisibleItemIndex by remember {
        derivedStateOf { state.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
      }
      LaunchedEffect(lastVisibleItemIndex) {
        lastVisibleItemIndex?.let { lastVisibleItemIndex ->
          onViewportChanged!!(state.firstVisibleItemIndex, lastVisibleItemIndex)
        }
      }

      val modifier = Modifier
        .run { if (width == Constraint.Fill) fillMaxWidth() else this }
        .run { if (height == Constraint.Fill) fillMaxHeight() else this }
        .pullRefresh(state = refreshState, enabled = onRefresh != null)
      if (isVertical) {
        LazyColumn(
          modifier = modifier,
          state = state,
          horizontalAlignment = Alignment.CenterHorizontally,
          content = content,
        )
      } else {
        LazyRow(
          modifier = modifier,
          state = state,
          verticalAlignment = Alignment.CenterVertically,
          content = content,
        )
      }
    }
  }
}
