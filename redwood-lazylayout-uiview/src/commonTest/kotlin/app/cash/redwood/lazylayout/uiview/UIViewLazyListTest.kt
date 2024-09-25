/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.lazylayout.uiview

import app.cash.redwood.lazylayout.AbstractLazyListTest
import app.cash.redwood.snapshot.testing.UIViewSnapshotCallback
import app.cash.redwood.snapshot.testing.UIViewSnapshotter
import app.cash.redwood.snapshot.testing.UIViewTestWidgetFactory
import platform.UIKit.UIView

class UIViewLazyListTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractLazyListTest<UIView>() {
  override val widgetFactory = UIViewTestWidgetFactory

  private val lazyLayoutWidgetFactory = UIViewRedwoodLazyLayoutWidgetFactory()

  override fun lazyList(backgroundColor: Int) = lazyLayoutWidgetFactory.LazyList()

  override fun snapshotter(widget: UIView) = UIViewSnapshotter.framed(callback, widget)
}
