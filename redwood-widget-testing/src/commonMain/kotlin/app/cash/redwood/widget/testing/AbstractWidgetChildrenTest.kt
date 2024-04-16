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
package app.cash.redwood.widget.testing

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isSameInstanceAs
import kotlin.test.Test

/** A set of conformance tests for [Widget.Children] implementations. */
public abstract class AbstractWidgetChildrenTest<W : Any> {
  /**
   * The [Widget.Children] test subject.
   *
   * Note: The value returned by this property **must** not change between calls.
   */
  public abstract val children: Widget.Children<W>

  /**
   * Create a new widget with the given [name]. This may be attached to [children],
   * and the name will need to be returned from [names] if so.
   */
  public abstract fun widget(name: String): W

  /** Return the ordered list of names for the widgets currently attached to [children]. */
  public abstract fun names(): List<String>

  /** @suppress */
  @Test public fun childrenInstanceDoesNotChange() {
    // This invokes the getter twice checking for accidental `get()` usage.
    assertThat(children).isSameInstanceAs(children)
  }

  /** @suppress */
  @Test public fun insertAppend() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    assertThat(names()).containsExactly("one", "two", "three")
  }

  /** @suppress */
  @Test public fun insertPrepend() {
    children.insert(0, "one")
    children.insert(0, "two")
    children.insert(0, "three")
    assertThat(names()).containsExactly("three", "two", "one")
  }

  /** @suppress */
  @Test public fun insertRandom() {
    children.insert(0, "one")
    children.insert(0, "two")
    children.insert(1, "three")
    assertThat(names()).containsExactly("two", "three", "one")
  }

  /** @suppress */
  @Test public fun moveSingleForward() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.move(0, 3, 1)
    assertThat(names()).containsExactly("two", "three", "one")
  }

  /** @suppress */
  @Test public fun moveSingleComposeApplierDocumentation() {
    // This test comes from Compose Applier's `move` documentation.
    children.insert(0, "A")
    children.insert(1, "B")
    children.insert(2, "C")
    children.insert(3, "D")
    children.insert(4, "E")
    children.move(1, 3, 1)
    assertThat(names()).containsExactly("A", "C", "B", "D", "E")
  }

  /** @suppress */
  @Test public fun moveMultipleForward() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.move(0, 3, 2)
    assertThat(names()).containsExactly("three", "one", "two")
  }

  /** @suppress */
  @Test public fun moveSingleBackward() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.move(2, 0, 1)
    assertThat(names()).containsExactly("three", "one", "two")
  }

  /** @suppress */
  @Test public fun moveMultipleBackward() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.move(1, 0, 2)
    assertThat(names()).containsExactly("two", "three", "one")
  }

  /** @suppress */
  @Test public fun moveZero() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.move(0, 1, 0)
    assertThat(names()).containsExactly("one", "two", "three")
    children.move(1, 0, 0)
    assertThat(names()).containsExactly("one", "two", "three")
  }

  /** @suppress */
  @Test public fun removeSingleStart() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.remove(0, 1)
    assertThat(names()).containsExactly("two", "three")
  }

  /** @suppress */
  @Test public fun removeMultipleStart() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.remove(0, 2)
    assertThat(names()).containsExactly("three")
  }

  /** @suppress */
  @Test public fun removeSingleEnd() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.remove(2, 1)
    assertThat(names()).containsExactly("one", "two")
  }

  /** @suppress */
  @Test public fun removeMultipleEnd() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.remove(1, 2)
    assertThat(names()).containsExactly("one")
  }

  /** @suppress */
  @Test public fun removeZero() {
    children.insert(0, "one")
    children.insert(1, "two")
    children.insert(2, "three")
    children.remove(1, 0)
    assertThat(names()).containsExactly("one", "two", "three")
  }

  /** @suppress */
  @Test public fun replace() {
    // Models what happens when a conditional flips from the second branch to the first.
    // The new item will be added and then the old item will be removed.
    // From https://github.com/cashapp/redwood/pull/1706.
    children.insert(0, "one")
    children.insert(0, "new one")
    children.remove(1, 1)
    assertThat(names()).containsExactly("new one")
  }

  private fun Widget.Children<W>.insert(index: Int, name: String) {
    insert(
      index = index,
      widget = object : Widget<W> {
        override val value: W = widget(name)
        override var modifier: Modifier = Modifier
      },
    )
  }
}
