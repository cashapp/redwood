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
package app.cash.redwood.protocol.widget

import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.widget.Widget
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProtocolDisplayTest {
  @Test fun rootWidgetMustHaveRootChildrenTag() {
    ProtocolDisplay(RootWidgetWithChildren, NullWidgetFactory) { }

    assertFailsWith<IllegalArgumentException> {
      ProtocolDisplay(RootWidgetChildrenThrows, NullWidgetFactory) { }
    }

    // Calls to children() are allowed to return null (usually from a ProtocolMismatchHandler)
    // so ensure that this very important case still causes an exception given that behavior.
    assertFailsWith<IllegalArgumentException> {
      ProtocolDisplay(RootWidgetChildrenNull, NullWidgetFactory) { }
    }
  }

  private object RootWidgetChildrenThrows : DiffConsumingWidget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff, eventSink: EventSink) = throw UnsupportedOperationException()
    override fun children(tag: Int) = throw IllegalArgumentException()
  }

  private object RootWidgetChildrenNull : DiffConsumingWidget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff, eventSink: EventSink) = throw UnsupportedOperationException()
    override fun children(tag: Int) = null
  }

  private object RootWidgetWithChildren : DiffConsumingWidget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff, eventSink: EventSink) = throw UnsupportedOperationException()
    override fun children(tag: Int) = when (tag) {
      RootChildrenTag -> NullWidgetChildren
      else -> throw IllegalArgumentException()
    }
  }

  private object NullWidgetFactory : DiffConsumingWidget.Factory<Unit> {
    override fun create(kind: Int) = throw UnsupportedOperationException()
  }

  private object NullWidgetChildren : Widget.Children<Unit> {
    override fun insert(index: Int, widget: Unit) = throw UnsupportedOperationException()
    override fun move(fromIndex: Int, toIndex: Int, count: Int) = throw UnsupportedOperationException()
    override fun remove(index: Int, count: Int) = throw UnsupportedOperationException()
    override fun clear() = throw UnsupportedOperationException()
  }
}
