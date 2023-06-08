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
package app.cash.redwood.layout.uiview

import app.cash.redwood.ui.dp
import app.cash.redwood.yoga.FlexDirection.Companion.Row
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIView

class UIViewFlexContainerTest {
  @Ignore // Ignore temporarily during the migration.
  @Test fun litmus() {
    val viewA = UIViewSpacer(10.0, 10.0)
    val viewB = UIViewSpacer(10.0, 10.0)
    val viewC = UIViewSpacer(10.0, 10.0)

    val container = UIViewFlexContainer(Row)
    container.children.apply {
      insert(0, viewA)
      insert(1, viewB)
    }

    val view = container.value
    view.setBounds(CGRectMake(0.0, 0.0, 40.0, 10.0))

    view.layoutIfNeeded()
    assertThat(viewA.value).hasFrame(0.0, 0.0, 10.0, 10.0)
    assertThat(viewB.value).hasFrame(10.0, 0.0, 10.0, 10.0)

    container.children.insert(2, viewC)

    view.layoutIfNeeded()
    assertThat(viewA.value).hasFrame(0.0, 0.0, 10.0, 10.0)
    assertThat(viewB.value).hasFrame(10.0, 0.0, 10.0, 10.0)
    assertThat(viewC.value).hasFrame(20.0, 0.0, 10.0, 10.0)

    viewB.width(20.0.dp)

    view.layoutIfNeeded()
    assertThat(viewA.value).hasFrame(0.0, 0.0, 10.0, 10.0)
    assertThat(viewB.value).hasFrame(10.0, 0.0, 20.0, 10.0)
    assertThat(viewC.value).hasFrame(30.0, 0.0, 10.0, 10.0)
  }

  private fun UIViewSpacer(width: Double, height: Double) = UIViewSpacer().apply {
    width(width.dp)
    height(height.dp)
  }

  private fun Assert<UIView>.hasFrame(x: Double, y: Double, width: Double, height: Double) {
    prop(UIView::frame).all {
      prop("x") { it.useContents { origin.x } }.isEqualTo(x)
      prop("y") { it.useContents { origin.y } }.isEqualTo(y)
      prop("width") { it.useContents { size.width } }.isEqualTo(width)
      prop("height") { it.useContents { size.height } }.isEqualTo(height)
    }
  }
}
