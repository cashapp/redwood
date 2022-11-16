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
package app.cash.zipline.samples.emojisearch.composeui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.composeui.TreehouseContent
import example.schema.widget.LazyColumn
import example.values.LazyListIntervalContent

class ComposeUiLazyColumn<T : Any>(
  treehouseApp: TreehouseApp<T>,
) : LazyColumn<@Composable () -> Unit> {
  private var intervals by mutableStateOf<List<LazyListIntervalContent>>(emptyList())

  override var layoutModifiers: LayoutModifier = LayoutModifier

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
          Box(Modifier.height(64.dp)) {
            TreehouseContent(treehouseApp) { interval.itemProvider.get(index) }
          }
        }
      }
    }
  }
}
