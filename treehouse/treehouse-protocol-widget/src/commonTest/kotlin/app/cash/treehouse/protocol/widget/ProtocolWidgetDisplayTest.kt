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
package app.cash.treehouse.protocol.widget

import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.treehouse.protocol.EventSink
import app.cash.treehouse.protocol.PropertyDiff
import app.cash.treehouse.widget.WidgetChildren
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProtocolWidgetDisplayTest {
  @Test fun rootWidgetMustHaveRootChildrenTag() {
    ProtocolDisplay(GoodRootWidget, NullWidgetFactory) { }

    assertFailsWith<IllegalArgumentException> {
      ProtocolDisplay(BadRootWidget, NullWidgetFactory) { }
    }
  }

  private object BadRootWidget : ProtocolWidget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff, eventSink: EventSink) = throw UnsupportedOperationException()
    override fun children(tag: Int) = throw IllegalArgumentException()
  }

  private object GoodRootWidget : ProtocolWidget<Unit> {
    override val value get() = Unit
    override fun apply(diff: PropertyDiff, eventSink: EventSink) = throw UnsupportedOperationException()
    override fun children(tag: Int) = when (tag) {
      RootChildrenTag -> NullWidgetChildren
      else -> throw IllegalArgumentException()
    }
  }

  private object NullWidgetFactory : ProtocolWidget.Factory<Unit> {
    override fun create(kind: Int) = throw UnsupportedOperationException()
  }

  private object NullWidgetChildren : WidgetChildren<Unit> {
    override fun insert(index: Int, widget: Unit) = throw UnsupportedOperationException()
    override fun move(fromIndex: Int, toIndex: Int, count: Int) = throw UnsupportedOperationException()
    override fun remove(index: Int, count: Int) = throw UnsupportedOperationException()
    override fun clear() = throw UnsupportedOperationException()
  }
}
