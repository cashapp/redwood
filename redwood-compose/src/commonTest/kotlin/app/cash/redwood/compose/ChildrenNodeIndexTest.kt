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
package app.cash.redwood.compose

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

/**
 * This class tests an implementation detail of [ChildrenNode] which maintains the correct
 * index value on its child [WidgetNode]s. While it could conceivably be tested through public
 * API it's far easier to validate the behavior this way.
 */
@OptIn(RedwoodCodegenApi::class)
class ChildrenNodeIndexTest {
  private val root = ChildrenNode(MutableListChildren<String>())

  @Test fun insert() {
    val a = WidgetNode(NoOpRedwoodApplier, StringWidget("a"))
    assertThat(a.index).isEqualTo(-1)
    root.insert(0, a)
    assertThat(a.index).isEqualTo(0)

    val b = WidgetNode(NoOpRedwoodApplier, StringWidget("a"))
    root.insert(1, b)
    assertThat(a.index).isEqualTo(0)
    assertThat(b.index).isEqualTo(1)

    val c = WidgetNode(NoOpRedwoodApplier, StringWidget("a"))
    root.insert(0, c)
    assertThat(c.index).isEqualTo(0)
    assertThat(a.index).isEqualTo(1)
    assertThat(b.index).isEqualTo(2)
  }

  @Test fun remove() {
    val a = WidgetNode(NoOpRedwoodApplier, StringWidget("a"))
    val b = WidgetNode(NoOpRedwoodApplier, StringWidget("b"))
    val c = WidgetNode(NoOpRedwoodApplier, StringWidget("c"))
    val d = WidgetNode(NoOpRedwoodApplier, StringWidget("d"))
    val e = WidgetNode(NoOpRedwoodApplier, StringWidget("e"))
    root.insert(0, a)
    root.insert(1, b)
    root.insert(2, c)
    root.insert(3, d)
    root.insert(4, e)
    assertThat(a.index).isEqualTo(0)
    assertThat(b.index).isEqualTo(1)
    assertThat(c.index).isEqualTo(2)
    assertThat(d.index).isEqualTo(3)
    assertThat(e.index).isEqualTo(4)

    root.remove(2, 1) // c
    assertThat(a.index).isEqualTo(0)
    assertThat(b.index).isEqualTo(1)
    assertThat(d.index).isEqualTo(2)
    assertThat(e.index).isEqualTo(3)

    root.remove(1, 2) // b, d
    assertThat(a.index).isEqualTo(0)
    assertThat(e.index).isEqualTo(1)

    root.remove(1, 1) // e
    assertThat(a.index).isEqualTo(0)
  }

  @Test fun move() {
    val a = WidgetNode(NoOpRedwoodApplier, StringWidget("a"))
    val b = WidgetNode(NoOpRedwoodApplier, StringWidget("b"))
    val c = WidgetNode(NoOpRedwoodApplier, StringWidget("c"))
    val d = WidgetNode(NoOpRedwoodApplier, StringWidget("d"))
    val e = WidgetNode(NoOpRedwoodApplier, StringWidget("e"))
    root.insert(0, a)
    root.insert(1, b)
    root.insert(2, c)
    root.insert(3, d)
    root.insert(4, e)
    assertThat(a.index).isEqualTo(0)
    assertThat(b.index).isEqualTo(1)
    assertThat(c.index).isEqualTo(2)
    assertThat(d.index).isEqualTo(3)
    assertThat(e.index).isEqualTo(4)

    root.move(0, 5, 1) // a 0 --> 4
    assertThat(b.index).isEqualTo(0)
    assertThat(c.index).isEqualTo(1)
    assertThat(d.index).isEqualTo(2)
    assertThat(e.index).isEqualTo(3)
    assertThat(a.index).isEqualTo(4)

    root.move(1, 4, 2) // c,d 1 --> 2
    assertThat(b.index).isEqualTo(0)
    assertThat(e.index).isEqualTo(1)
    assertThat(c.index).isEqualTo(2)
    assertThat(d.index).isEqualTo(3)
    assertThat(a.index).isEqualTo(4)

    root.move(2, 1, 3) // c,d,a 2 --> 1
    assertThat(b.index).isEqualTo(0)
    assertThat(c.index).isEqualTo(1)
    assertThat(d.index).isEqualTo(2)
    assertThat(a.index).isEqualTo(3)
    assertThat(e.index).isEqualTo(4)
  }
}

private class StringWidget(override val value: String) : Widget<String> {
  override var modifier: Modifier = Modifier
}

@OptIn(RedwoodCodegenApi::class)
private object NoOpRedwoodApplier : RedwoodApplier<String> {
  override val widgetSystem get() = throw UnsupportedOperationException()
  override fun recordChanged(widget: Widget<String>) = Unit
}
