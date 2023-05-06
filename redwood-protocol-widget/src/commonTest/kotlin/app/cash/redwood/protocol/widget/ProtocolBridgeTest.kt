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
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.MutableListChildren
import assertk.assertThat
import assertk.assertions.hasMessage
import example.redwood.widget.ExampleSchemaProtocolNodeFactory
import example.redwood.widget.ExampleSchemaWidgetFactories
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.JsonPrimitive

class ProtocolBridgeTest {
  @Test fun insertRootIdThrows() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = ExampleSchemaProtocolNodeFactory(
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
    assertThat(t).hasMessage("Insert attempted to replace existing widget with ID 0")
  }

  @Test fun duplicateIdThrows() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = ExampleSchemaProtocolNodeFactory(
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
    assertThat(t).hasMessage("Insert attempted to replace existing widget with ID 1")
  }

  @Test fun removeRemoves() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = ExampleSchemaProtocolNodeFactory(
        provider = ExampleSchemaWidgetFactories(
          ExampleSchema = EmptyExampleSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )

    // Add a button.
    bridge.sendDiff(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Insert(
            id = Id.Root,
            tag = ChildrenTag.Root,
            childId = Id(1),
            widgetTag = WidgetTag(4) /* button */,
            index = 0,
          ),
        ),
      ),
    )

    // Remove the button.
    bridge.sendDiff(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Remove(
            id = Id.Root,
            tag = ChildrenTag.Root,
            index = 0,
            count = 1,
            removedIds = listOf(Id(1)),
          ),
        ),
      ),
    )

    // Ensure targeting the button fails.
    val updateButtonText = Diff(
      propertyDiffs = listOf(
        PropertyDiff(
          id = Id(1),
          tag = PropertyTag(1) /* text */,
          value = JsonPrimitive("hello"),
        ),
      ),
    )
    val t = assertFailsWith<IllegalStateException> {
      bridge.sendDiff(updateButtonText)
    }
    assertThat(t).hasMessage("Unknown widget ID 1")
  }
}
