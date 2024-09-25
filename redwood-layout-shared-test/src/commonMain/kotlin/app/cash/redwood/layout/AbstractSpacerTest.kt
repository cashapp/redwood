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
package app.cash.redwood.layout

import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.ui.dp
import app.cash.redwood.widget.Widget
import kotlin.test.Test

abstract class AbstractSpacerTest<T : Any> {

  abstract fun widget(): Spacer<T>

  abstract fun wrap(widget: Widget<T>, horizontal: Boolean): T

  abstract fun snapshotter(widget: T): Snapshotter

  private fun widget(width: Int, height: Int): Spacer<T> = widget().apply {
    width(width.dp)
    height(height.dp)
  }

  @Test fun testZeroSpacer() {
    val widget = widget(width = 0, height = 0)
    snapshotter(wrap(widget, horizontal = true)).snapshot()
  }

  @Test fun testWidthOnlySpacer() {
    val widget = widget(width = 100, height = 0)
    snapshotter(wrap(widget, horizontal = true)).snapshot()
  }

  @Test fun testHeightOnlySpacer() {
    val widget = widget(width = 0, height = 100)
    snapshotter(wrap(widget, horizontal = false)).snapshot()
  }

  @Test fun testBothSpacer() {
    val widget = widget(width = 100, height = 100)
    snapshotter(wrap(widget, horizontal = false)).snapshot()
  }
}
