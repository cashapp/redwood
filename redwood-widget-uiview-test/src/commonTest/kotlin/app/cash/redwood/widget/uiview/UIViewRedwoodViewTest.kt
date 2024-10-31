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
package app.cash.redwood.widget.uiview

import app.cash.redwood.snapshot.testing.UIViewSnapshotCallback
import app.cash.redwood.snapshot.testing.UIViewSnapshotter
import app.cash.redwood.snapshot.testing.UIViewTestWidgetFactory
import app.cash.redwood.widget.AbstractRedwoodViewTest
import app.cash.redwood.widget.RedwoodUIView
import app.cash.redwood.widget.Widget
import platform.UIKit.UIView

class UIViewRedwoodViewTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractRedwoodViewTest<UIView, RedwoodUIView>() {
  override val widgetFactory = UIViewTestWidgetFactory

  override fun redwoodView() = RedwoodUIView()

  override fun snapshotter(redwoodView: RedwoodUIView) =
    UIViewSnapshotter.framed(callback, redwoodView.value)

  override fun snapshotter(widget: Widget<UIView>) =
    UIViewSnapshotter.framed(callback, widget.value)
}
