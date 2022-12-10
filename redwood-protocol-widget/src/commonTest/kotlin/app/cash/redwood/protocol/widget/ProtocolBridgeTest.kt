/*
 * Copyright (C) 2022 Square, Inc.
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

import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.MutableListChildren
import example.redwood.widget.ExampleSchemaDiffConsumingNodeFactory
import example.redwood.widget.ExampleSchemaWidgetFactories
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProtocolBridgeTest {
  @Test fun insertRootIdThrows() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = ExampleSchemaDiffConsumingNodeFactory(
        provider = ExampleSchemaWidgetFactories(
          ExampleSchema = EmptyExampleSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )
    val diff = Diff(
      childrenDiffs = listOf(
        ChildrenDiff.Insert(
          id = Id.Root,
          tag = ChildrenTag.Root,
          childId = Id.Root,
          widgetTag = WidgetTag(4) /* button */,
          index = 0,
        ),
      ),
    )
    val t = assertFailsWith<IllegalArgumentException> {
      bridge.sendDiff(diff)
    }
    assertEquals("Insert attempted to replace existing widget with ID 0", t.message)
  }

  @Test fun duplicateIdThrows() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = ExampleSchemaDiffConsumingNodeFactory(
        provider = ExampleSchemaWidgetFactories(
          ExampleSchema = EmptyExampleSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )
    val diff = Diff(
      childrenDiffs = listOf(
        ChildrenDiff.Insert(
          id = Id.Root,
          tag = ChildrenTag.Root,
          childId = Id(1),
          widgetTag = WidgetTag(4) /* button */,
          index = 0,
        ),
      ),
    )
    bridge.sendDiff(diff)
    val t = assertFailsWith<IllegalArgumentException> {
      bridge.sendDiff(diff)
    }
    assertEquals("Insert attempted to replace existing widget with ID 1", t.message)
  }
}
