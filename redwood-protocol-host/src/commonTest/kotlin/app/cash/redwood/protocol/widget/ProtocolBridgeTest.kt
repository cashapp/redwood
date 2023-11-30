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

import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenChange.Remove
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.MutableListChildren
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.example.redwood.testing.widget.TestSchemaProtocolFactory
import com.example.redwood.testing.widget.TestSchemaWidgetFactories
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.JsonPrimitive

class ProtocolBridgeTest {
  @Test fun createRootIdThrows() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = TestSchemaProtocolFactory(
        provider = TestSchemaWidgetFactories(
          TestSchema = EmptyTestSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
          RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )
    val changes = listOf(
      Create(
        id = Id.Root,
        // Button
        tag = WidgetTag(4),
      ),
    )
    val t = assertFailsWith<IllegalArgumentException> {
      bridge.sendChanges(changes)
    }
    assertThat(t).hasMessage("Insert attempted to replace existing widget with ID 0")
  }

  @Test fun duplicateIdThrows() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = TestSchemaProtocolFactory(
        provider = TestSchemaWidgetFactories(
          TestSchema = EmptyTestSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
          RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )
    val changes = listOf(
      Create(
        id = Id(1),
        // Button
        tag = WidgetTag(4),
      ),
    )
    bridge.sendChanges(changes)
    val t = assertFailsWith<IllegalArgumentException> {
      bridge.sendChanges(changes)
    }
    assertThat(t).hasMessage("Insert attempted to replace existing widget with ID 1")
  }

  @Test fun removeRemoves() {
    val bridge = ProtocolBridge(
      container = MutableListChildren(),
      factory = TestSchemaProtocolFactory(
        provider = TestSchemaWidgetFactories(
          TestSchema = EmptyTestSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
          RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )

    // Add a button.
    bridge.sendChanges(
      listOf(
        Create(
          id = Id(1),
          // Button
          tag = WidgetTag(4),
        ),
        Add(
          id = Id.Root,
          tag = ChildrenTag.Root,
          childId = Id(1),
          index = 0,
        ),
      ),
    )

    // Remove the button.
    bridge.sendChanges(
      listOf(
        Remove(
          id = Id.Root,
          tag = ChildrenTag.Root,
          index = 0,
          count = 1,
          removedIds = listOf(Id(1)),
        ),
      ),
    )

    // Ensure targeting the button fails.
    val updateButtonText = listOf(
      PropertyChange(
        id = Id(1),
        // text
        tag = PropertyTag(1),
        value = JsonPrimitive("hello"),
      ),
    )
    val t = assertFailsWith<IllegalStateException> {
      bridge.sendChanges(updateButtonText)
    }
    assertThat(t).hasMessage("Unknown widget ID 1")
  }

  @Test fun modifierChangeNotifiesContainer() {
    var modifierUpdateCount = 0
    val bridge = ProtocolBridge(
      container = MutableListChildren(modifierUpdated = { modifierUpdateCount++ }),
      factory = TestSchemaProtocolFactory(
        provider = TestSchemaWidgetFactories(
          TestSchema = EmptyTestSchemaWidgetFactory(),
          RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
          RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
        ),
      ),
      eventSink = ::error,
    )

    // Initial Button add does not trigger update callback (it's implicit because of insert).
    bridge.sendChanges(
      listOf(
        // Button
        Create(Id(1), WidgetTag(4)),
        ModifierChange(Id(1)),
        Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )
    assertThat(modifierUpdateCount).isEqualTo(0)

    // Future modifier changes trigger the callback.
    bridge.sendChanges(
      listOf(
        ModifierChange(Id(1)),
      ),
    )
    assertThat(modifierUpdateCount).isEqualTo(1)
  }
}
