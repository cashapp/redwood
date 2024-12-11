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
package app.cash.redwood.protocol.host

import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenChange.Remove
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.guest.guestRedwoodVersion
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.widget.MutableListChildren
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.message
import com.example.redwood.testapp.protocol.host.TestSchemaProtocolFactory
import com.example.redwood.testapp.testing.TestRowValue
import com.example.redwood.testapp.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testapp.testing.TextValue
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.JsonPrimitive

@OptIn(RedwoodCodegenApi::class)
class HostProtocolAdapterTest {
  @Test fun createRootIdThrows() {
    val hostAdapter = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = MutableListChildren(),
      factory = TestSchemaProtocolFactory(
        widgetSystem = TestSchemaWidgetSystem(
          TestSchema = TestSchemaTestingWidgetFactory(),
          RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
          RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
        ),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )
    val changes = listOf(
      Create(
        id = Id.Root,
        // Button
        tag = WidgetTag(4),
      ),
    )
    val t = assertFailsWith<IllegalArgumentException> {
      hostAdapter.sendChanges(changes)
    }
    assertThat(t).hasMessage("Insert attempted to replace existing widget with ID 0")
  }

  @Test fun duplicateIdThrows() {
    val hostAdapter = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = MutableListChildren(),
      factory = TestSchemaProtocolFactory(
        widgetSystem = TestSchemaWidgetSystem(
          TestSchema = TestSchemaTestingWidgetFactory(),
          RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
          RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
        ),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )
    val changes = listOf(
      Create(
        id = Id(1),
        // Button
        tag = WidgetTag(4),
      ),
    )
    hostAdapter.sendChanges(changes)
    val t = assertFailsWith<IllegalArgumentException> {
      hostAdapter.sendChanges(changes)
    }
    assertThat(t).hasMessage("Insert attempted to replace existing widget with ID 1")
  }

  @Test fun removeRemoves() {
    val hostAdapter = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = MutableListChildren(),
      factory = TestSchemaProtocolFactory(
        widgetSystem = TestSchemaWidgetSystem(
          TestSchema = TestSchemaTestingWidgetFactory(),
          RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
          RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
        ),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )

    // Add a button.
    hostAdapter.sendChanges(
      listOf(
        Create(
          id = Id(1),
          // Button
          tag = WidgetTag(4),
        ),
        // Set Button's required color property.
        PropertyChange(
          id = Id(1),
          widgetTag = WidgetTag(4),
          propertyTag = PropertyTag(3),
          value = JsonPrimitive(0),
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
    hostAdapter.sendChanges(
      listOf(
        Remove(
          id = Id.Root,
          tag = ChildrenTag.Root,
          index = 0,
          detach = false,
        ),
      ),
    )

    // Ensure targeting the button fails.
    val updateButtonText = listOf(
      PropertyChange(
        id = Id(1),
        widgetTag = WidgetTag(4),
        // text
        propertyTag = PropertyTag(1),
        value = JsonPrimitive("hello"),
      ),
    )
    val t = assertFailsWith<IllegalStateException> {
      hostAdapter.sendChanges(updateButtonText)
    }
    assertThat(t).hasMessage("Unknown widget ID 1")
  }

  @Test fun modifierChangeNotifiesContainer() {
    var modifierUpdateCount = 0
    val hostAdapter = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = MutableListChildren(modifierUpdated = { modifierUpdateCount++ }),
      factory = TestSchemaProtocolFactory(
        widgetSystem = TestSchemaWidgetSystem(
          TestSchema = TestSchemaTestingWidgetFactory(),
          RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
          RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
        ),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )

    // Initial Button add does not trigger update callback (it's implicit because of insert).
    hostAdapter.sendChanges(
      listOf(
        // Button
        Create(Id(1), WidgetTag(4)),
        ModifierChange(Id(1)),
        Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )
    assertThat(modifierUpdateCount).isEqualTo(0)

    // Future modifier changes trigger the callback.
    hostAdapter.sendChanges(
      listOf(
        ModifierChange(Id(1)),
      ),
    )
    assertThat(modifierUpdateCount).isEqualTo(1)
  }

  @Test fun entireSubtreeRemoved() {
    val container = MutableListChildren<WidgetValue>()
    val host = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = container,
      factory = TestSchemaProtocolFactory(
        widgetSystem = TestSchemaWidgetSystem(
          TestSchema = TestSchemaTestingWidgetFactory(),
          RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
          RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
        ),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )

    // TestRow {
    //   TestRow {
    //     Text("hello")
    host.sendChanges(
      listOf(
        // TestRow
        Create(Id(1), WidgetTag(1)),
        ModifierChange(Id(1)),
        // TestRow
        Create(Id(2), WidgetTag(1)),
        ModifierChange(Id(2)),
        // Text
        Create(Id(3), WidgetTag(3)),
        PropertyChange(Id(3), WidgetTag(3), PropertyTag(1), JsonPrimitive("hello")),
        ModifierChange(Id(3)),
        Add(Id(2), ChildrenTag(1), Id(3), 0),
        Add(Id(1), ChildrenTag(1), Id(2), 0),
        Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )

    // Validate we're tracking ID=3.
    host.sendChanges(
      listOf(
        PropertyChange(Id(3), WidgetTag(3), PropertyTag(1), JsonPrimitive("hey")),
      ),
    )

    // Remove root TestRow.
    host.sendChanges(
      listOf(
        Remove(Id.Root, ChildrenTag.Root, 0, false),
      ),
    )

    assertFailure {
      host.sendChanges(
        listOf(
          PropertyChange(Id(3), WidgetTag(3), PropertyTag(1), JsonPrimitive("sup")),
        ),
      )
    }.isInstanceOf<IllegalStateException>()
      .message()
      .isEqualTo("Unknown widget ID 3")
  }

  @Test fun detachAndAdd() {
    val container = MutableListChildren<WidgetValue>()
    val host = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = container,
      factory = TestSchemaProtocolFactory(
        widgetSystem = TestSchemaWidgetSystem(
          TestSchema = TestSchemaTestingWidgetFactory(),
          RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
          RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
        ),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )

    // TestRow {
    //   TestRow {
    //     Text("hello")
    host.sendChanges(
      listOf(
        // TestRow
        Create(Id(1), WidgetTag(1)),
        ModifierChange(Id(1)),
        // TestRow
        Create(Id(2), WidgetTag(1)),
        ModifierChange(Id(2)),
        // Text
        Create(Id(3), WidgetTag(3)),
        PropertyChange(Id(3), WidgetTag(3), PropertyTag(1), JsonPrimitive("hello")),
        ModifierChange(Id(3)),
        Add(Id(2), ChildrenTag(1), Id(3), 0),
        Add(Id(1), ChildrenTag(1), Id(2), 0),
        Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )

    host.sendChanges(
      listOf(
        // Detach inner TestRow.
        Remove(Id(1), ChildrenTag(1), 0, detach = true),
        // Remove outer TestRow.
        Remove(Id.Root, ChildrenTag.Root, 0, detach = false),
        // New outer TestRow.
        Create(Id(4), WidgetTag(1)),
        ModifierChange(Id(4)),
        Add(Id.Root, ChildrenTag.Root, Id(4), 0),
        // Re-attach inner TestRow.
        Add(Id(4), ChildrenTag(1), Id(2), 0),
      ),
    )

    assertThat(container.single().value).isEqualTo(
      TestRowValue(
        children = listOf(
          TestRowValue(
            children = listOf(
              TextValue(text = "hello"),
            ),
          ),
        ),
      ),
    )
  }
}
