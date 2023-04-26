/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.widget

import app.cash.redwood.LayoutModifier
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

abstract class AbstractWidgetChildrenTest<W : Any> {
  abstract val children: Widget.Children<W>
  abstract fun widget(name: String): W
  abstract fun names(): List<String>

  @Test fun insertAppend() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    assertThat(names()).containsExactly("one", "two", "three")
  }

  @Test fun insertPrepend() {
    children.insert(0, widget("one"))
    children.insert(0, widget("two"))
    children.insert(0, widget("three"))
    assertThat(names()).containsExactly("three", "two", "one")
  }

  @Test fun insertRandom() {
    children.insert(0, widget("one"))
    children.insert(0, widget("two"))
    children.insert(1, widget("three"))
    assertThat(names()).containsExactly("two", "three", "one")
  }

  @Test fun moveSingleForward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(0, 3, 1)
    assertThat(names()).containsExactly("two", "three", "one")
  }

  @Test fun moveSingleComposeApplierDocumentation() {
    // This test comes from Compose Applier's `move` documentation.
    children.insert(0, widget("A"))
    children.insert(1, widget("B"))
    children.insert(2, widget("C"))
    children.insert(3, widget("D"))
    children.insert(4, widget("E"))
    children.move(1, 3, 1)
    assertThat(names()).containsExactly("A", "C", "B", "D", "E")
  }

  @Test fun moveMultipleForward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(0, 3, 2)
    assertThat(names()).containsExactly("three", "one", "two")
  }

  @Test fun moveSingleBackward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(2, 0, 1)
    assertThat(names()).containsExactly("three", "one", "two")
  }

  @Test fun moveMultipleBackward() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(1, 0, 2)
    assertThat(names()).containsExactly("two", "three", "one")
  }

  @Test fun moveZero() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.move(0, 1, 0)
    assertThat(names()).containsExactly("one", "two", "three")
    children.move(1, 0, 0)
    assertThat(names()).containsExactly("one", "two", "three")
  }

  @Test fun removeSingleStart() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(0, 1)
    assertThat(names()).containsExactly("two", "three")
  }

  @Test fun removeMultipleStart() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(0, 2)
    assertThat(names()).containsExactly("three")
  }

  @Test fun removeSingleEnd() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(2, 1)
    assertThat(names()).containsExactly("one", "two")
  }

  @Test fun removeMultipleEnd() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(1, 2)
    assertThat(names()).containsExactly("one")
  }

  @Test fun removeZero() {
    children.insert(0, widget("one"))
    children.insert(1, widget("two"))
    children.insert(2, widget("three"))
    children.remove(1, 0)
    assertThat(names()).containsExactly("one", "two", "three")
  }

  private fun <W : Any> Widget.Children<W>.insert(index: Int, widget: W) {
    insert(
      index = index,
      widget = object : Widget<W> {
        override val value: W = widget
        override var layoutModifiers: LayoutModifier = LayoutModifier
      },
    )
  }
}
