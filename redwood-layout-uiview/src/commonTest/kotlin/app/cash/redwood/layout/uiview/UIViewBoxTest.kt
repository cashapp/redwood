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
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.AbstractBoxTest
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.snapshot.testing.UIViewSnapshotCallback
import app.cash.redwood.snapshot.testing.UIViewSnapshotter
import app.cash.redwood.snapshot.testing.UIViewTestWidgetFactory
import app.cash.redwood.snapshot.testing.isEqualTo
import assertk.assertThat
import kotlin.test.Test
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIColor
import platform.UIKit.UIView

class UIViewBoxTest(
  private val callback: UIViewSnapshotCallback,
) : AbstractBoxTest<UIView>() {

  override val widgetFactory = UIViewTestWidgetFactory

  override fun box(): Box<UIView> {
    return UIViewBox().apply {
      value.backgroundColor = UIColor(red = 0.0, green = 0.0, blue = 0.0, alpha = 0.5)
    }
  }

  override fun snapshotter(widget: UIView) = UIViewSnapshotter.framed(callback, widget)

  @Test
  fun maxEachDimension() {
    assertThat(
      maxEachDimension(
        CGSizeMake(5.0, 10.0),
        CGSizeMake(8.0, 4.0),
      ),
    ).isEqualTo(8.0, 10.0)

    assertThat(
      maxEachDimension(
        CGSizeMake(8.0, 4.0),
        CGSizeMake(5.0, 10.0),
      ),
    ).isEqualTo(8.0, 10.0)

    assertThat(
      maxEachDimension(
        CGSizeMake(8.0, 10.0),
        CGSizeMake(5.0, 4.0),
      ),
    ).isEqualTo(8.0, 10.0)

    assertThat(
      maxEachDimension(
        CGSizeMake(5.0, 4.0),
        CGSizeMake(8.0, 10.0),
      ),
    ).isEqualTo(8.0, 10.0)
  }
}
