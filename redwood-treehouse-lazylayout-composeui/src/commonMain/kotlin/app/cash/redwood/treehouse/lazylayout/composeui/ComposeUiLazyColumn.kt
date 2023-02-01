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
package app.cash.redwood.treehouse.lazylayout.composeui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.composeui.TreehouseContent
import app.cash.redwood.treehouse.lazylayout.api.LazyListIntervalContent
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import app.cash.redwood.widget.compose.ComposeWidgetChildren

internal class ComposeUiLazyColumn<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: TreehouseView.WidgetSystem<A>,
) : LazyColumn<@Composable () -> Unit> {
  private var intervals by mutableStateOf<List<LazyListIntervalContent>>(emptyList())

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val placeholder = ComposeWidgetChildren()

  override fun intervals(intervals: List<LazyListIntervalContent>) {
    this.intervals = intervals
  }

  override val value = @Composable {
    LazyColumn(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth(),
    ) {
      intervals.forEach { interval ->
        items(interval.count) { index ->
          var isPlaceholderVisible by remember { mutableStateOf(true) }
          if (isPlaceholderVisible) {
            placeholder.render()
          }
          val codeListener = remember {
            object : TreehouseView.CodeListener() {
              override fun onCodeLoaded(initial: Boolean) {
                isPlaceholderVisible = false
              }
            }
          }
          TreehouseContent(treehouseApp, widgetSystem, codeListener) { interval.itemProvider.get(index) }
        }
      }
    }
  }
}
