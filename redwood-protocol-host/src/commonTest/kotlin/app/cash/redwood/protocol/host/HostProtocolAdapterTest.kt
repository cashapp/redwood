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

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.leaks.LeakDetector
import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenChange.Remove
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.guest.guestRedwoodVersion
import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.ui.basic.testing.RedwoodUiBasicTestingWidgetFactory
import app.cash.redwood.ui.basic.testing.TextValue
import app.cash.redwood.widget.MutableListChildren
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.message
import com.example.redwood.testapp.protocol.host.TestSchemaHostProtocol
import com.example.redwood.testapp.testing.TestRowValue
import com.example.redwood.testapp.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testapp.widget.TestSchemaWidgetSystem
import kotlin.test.Test
import kotlin.test.assertFailsWith

@OptIn(RedwoodCodegenApi::class)
class HostProtocolAdapterTest {
  @Test fun createRootIdThrows() {
    val hostAdapter = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = MutableListChildren(),
      protocol = TestSchemaHostProtocol.create(),
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodUiBasic = RedwoodUiBasicTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )
    val changes = listOf(
      UiCreate(
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
      protocol = TestSchemaHostProtocol.create(),
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodUiBasic = RedwoodUiBasicTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )
    val changes = listOf(
      UiCreate(
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
      protocol = TestSchemaHostProtocol.create(),
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodUiBasic = RedwoodUiBasicTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )

    // Add a button.
    hostAdapter.sendChanges(
      listOf(
        UiCreate(
          id = Id(1),
          // Button
          tag = WidgetTag(4),
        ),
        // Set Button's required color property.
        UiPropertyChange(
          id = Id(1),
          tag = PropertyTag(3),
          value = 0U,
        ),
        UiChildrenChange(
          change = Add(
            id = Id.Root,
            tag = ChildrenTag.Root,
            childId = Id(1),
            index = 0,
          ),
        ),
      ),
    )

    // Remove the button.
    hostAdapter.sendChanges(
      listOf(
        UiChildrenChange(
          change = Remove(
            id = Id.Root,
            tag = ChildrenTag.Root,
            index = 0,
            detach = false,
          ),
        ),
      ),
    )

    // Ensure targeting the button fails.
    val updateButtonText = listOf(
      UiPropertyChange(
        id = Id(1),
        // text
        tag = PropertyTag(1),
        value = "hello",
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
      protocol = TestSchemaHostProtocol.create(),
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodUiBasic = RedwoodUiBasicTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      eventSink = ::error,
      leakDetector = LeakDetector.none(),
    )

    // Initial Button add does not trigger update callback (it's implicit because of insert).
    hostAdapter.sendChanges(
      listOf(
        // Button
        UiCreate(Id(1), WidgetTag(4)),
        UiModifierChange(Id(1), false, Modifier),
        UiChildrenChange(Add(Id.Root, ChildrenTag.Root, Id(1), 0)),
      ),
    )
    assertThat(modifierUpdateCount).isEqualTo(0)

    // Future modifier changes trigger the callback.
    hostAdapter.sendChanges(
      listOf(
        UiModifierChange(Id(1), false, Modifier),
      ),
    )
    assertThat(modifierUpdateCount).isEqualTo(1)
  }

  @Test fun entireSubtreeRemoved() {
    val container = MutableListChildren<WidgetValue>()
    val host = HostProtocolAdapter(
      guestVersion = guestRedwoodVersion,
      container = container,
      protocol = TestSchemaHostProtocol.create(),
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodUiBasic = RedwoodUiBasicTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
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
        UiCreate(Id(1), WidgetTag(1)),
        UiModifierChange(Id(1), false, Modifier),
        // TestRow
        UiCreate(Id(2), WidgetTag(1)),
        UiModifierChange(Id(2), false, Modifier),
        // Text
        UiCreate(Id(3), WidgetTag(1_000_003)),
        UiPropertyChange(Id(3), PropertyTag(1), "hello"),
        UiModifierChange(Id(3), false, Modifier),
        UiChildrenChange(Add(Id(2), ChildrenTag(1), Id(3), 0)),
        UiChildrenChange(Add(Id(1), ChildrenTag(1), Id(2), 0)),
        UiChildrenChange(Add(Id.Root, ChildrenTag.Root, Id(1), 0)),
      ),
    )

    // Validate we're tracking ID=3.
    host.sendChanges(
      listOf(
        UiPropertyChange(Id(3), PropertyTag(1), "hey"),
      ),
    )

    // Remove root TestRow.
    host.sendChanges(
      listOf(
        UiChildrenChange(Remove(Id.Root, ChildrenTag.Root, 0, false)),
      ),
    )

    assertFailure {
      host.sendChanges(
        listOf(
          UiPropertyChange(Id(3), PropertyTag(1), "sup"),
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
      protocol = TestSchemaHostProtocol.create(),
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodUiBasic = RedwoodUiBasicTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
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
        UiCreate(Id(1), WidgetTag(1)),
        UiModifierChange(Id(1), false, Modifier),
        // TestRow
        UiCreate(Id(2), WidgetTag(1)),
        UiModifierChange(Id(2), false, Modifier),
        // Text
        UiCreate(Id(3), WidgetTag(1_000_002)),
        UiPropertyChange(Id(3), PropertyTag(1), "hello"),
        UiModifierChange(Id(3), false, Modifier),
        UiChildrenChange(Add(Id(2), ChildrenTag(1), Id(3), 0)),
        UiChildrenChange(Add(Id(1), ChildrenTag(1), Id(2), 0)),
        UiChildrenChange(Add(Id.Root, ChildrenTag.Root, Id(1), 0)),
      ),
    )

    host.sendChanges(
      listOf(
        // Detach inner TestRow.
        UiChildrenChange(Remove(Id(1), ChildrenTag(1), 0, detach = true)),
        // Remove outer TestRow.
        UiChildrenChange(Remove(Id.Root, ChildrenTag.Root, 0, detach = false)),
        // New outer TestRow.
        UiCreate(Id(4), WidgetTag(1)),
        UiModifierChange(Id(4), false, Modifier),
        UiChildrenChange(Add(Id.Root, ChildrenTag.Root, Id(4), 0)),
        // Re-attach inner TestRow.
        UiChildrenChange(Add(Id(4), ChildrenTag(1), Id(2), 0)),
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
