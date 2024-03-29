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
package app.cash.redwood.testing

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.compose.current
import app.cash.redwood.layout.widget.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.widget.RedwoodLazyLayoutTestingWidgetFactory
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.guest.ProtocolRedwoodComposition
import app.cash.redwood.protocol.widget.ProtocolBridge
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.redwood.widget.MutableListChildren
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.example.redwood.testing.compose.TestRow
import com.example.redwood.testing.compose.TestSchemaProtocolBridge
import com.example.redwood.testing.compose.Text
import com.example.redwood.testing.widget.TestSchemaProtocolFactory
import com.example.redwood.testing.widget.TestSchemaTester
import com.example.redwood.testing.widget.TestSchemaTestingWidgetFactory
import com.example.redwood.testing.widget.TestSchemaWidgetSystem
import com.example.redwood.testing.widget.TextValue
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive

@OptIn(RedwoodCodegenApi::class)
class ViewTreesTest {
  @Test fun nested() = runTest {
    val content = @Composable {
      TestRow {
        TestRow {
          Text("One Fish")
          Text("Two Fish")
        }
        TestRow {
          Text("Red Fish")
          Text("Blue Fish")
        }
      }
    }

    val snapshot = TestSchemaTester {
      setContent(content)
      awaitSnapshot()
    }

    val expected = listOf(
      Create(Id(1), WidgetTag(1)),
      ModifierChange(Id(1), emptyList()),
      Create(Id(2), WidgetTag(1)),
      ModifierChange(Id(2), emptyList()),
      Create(Id(3), WidgetTag(3)),
      PropertyChange(Id(3), PropertyTag(1), JsonPrimitive("One Fish")),
      ModifierChange(Id(3), emptyList()),
      Add(Id(2), ChildrenTag(1), Id(3), 0),
      Create(Id(4), WidgetTag(3)),
      PropertyChange(Id(4), PropertyTag(1), JsonPrimitive("Two Fish")),
      ModifierChange(Id(4), emptyList()),
      Add(Id(2), ChildrenTag(1), Id(4), 1),
      Add(Id(1), ChildrenTag(1), Id(2), 0),
      Create(Id(5), WidgetTag(1)),
      ModifierChange(Id(5), emptyList()),
      Create(Id(6), WidgetTag(3)),
      PropertyChange(Id(6), PropertyTag(1), JsonPrimitive("Red Fish")),
      ModifierChange(Id(6), emptyList()),
      Add(Id(5), ChildrenTag(1), Id(6), 0),
      Create(Id(7), WidgetTag(3)),
      PropertyChange(Id(7), PropertyTag(1), JsonPrimitive("Blue Fish")),
      ModifierChange(Id(7), emptyList()),
      Add(Id(5), ChildrenTag(1), Id(7), 1),
      Add(Id(1), ChildrenTag(1), Id(5), 1),
      Add(Id.Root, ChildrenTag.Root, Id(1), 0),
    )

    // Ensure the normal view tree APIs produce the expected list of changes.
    assertThat(snapshot.toChangeList(TestSchemaProtocolBridge).changes)
      .isEqualTo(expected)
    assertThat(snapshot.single().toChangeList(TestSchemaProtocolBridge).changes)
      .isEqualTo(expected)

    // Validate that the normal Compose protocol backend produces the same list of changes.
    lateinit var protocolChanges: List<Change>
    val composition = ProtocolRedwoodComposition(
      scope = this + BroadcastFrameClock(),
      bridge = TestSchemaProtocolBridge.create(),
      changesSink = { protocolChanges = it },
      widgetVersion = UInt.MAX_VALUE,
      onBackPressedDispatcher = object : OnBackPressedDispatcher {
        override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
          return object : Cancellable {
            override fun cancel() = Unit
          }
        }
      },
      saveableStateRegistry = null,
      uiConfigurations = MutableStateFlow(UiConfiguration()),
    )
    composition.setContent(content)
    composition.cancel()

    assertThat(protocolChanges).isEqualTo(expected)

    // Ensure when the changes are applied with the widget protocol we get equivalent values.
    val widgetSystem = TestSchemaWidgetSystem(
      TestSchema = TestSchemaTestingWidgetFactory(),
      RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
      RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
    )
    val protocolNodes = TestSchemaProtocolFactory(widgetSystem)
    val widgetContainer = MutableListChildren<WidgetValue>()
    val widgetBridge = ProtocolBridge(widgetContainer, protocolNodes) {
      throw AssertionError()
    }
    widgetBridge.sendChanges(expected)

    assertThat(widgetContainer.map { it.value }).isEqualTo(snapshot)
  }

  @Test fun uiConfigurationWorks() = runTest {
    TestSchemaTester {
      setContent {
        Text("Dark: ${UiConfiguration.current.darkMode}")
      }

      val first = awaitSnapshot()
      assertThat(first).containsExactly(TextValue(text = "Dark: false"))

      uiConfigurations.value = UiConfiguration(darkMode = true)

      val second = awaitSnapshot()
      assertThat(second).containsExactly(TextValue(text = "Dark: true"))
    }
  }
}
