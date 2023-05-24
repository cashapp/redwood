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
package app.cash.redwood.protocol.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.redwood.compose.WidgetVersion
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.testing.TestRedwoodComposition
import assertk.assertThat
import assertk.assertions.isEqualTo
import example.redwood.compose.Button
import example.redwood.compose.ExampleSchemaProtocolBridge
import example.redwood.compose.Row
import example.redwood.compose.Text
import kotlin.test.Test
import kotlin.test.fail
import kotlinx.coroutines.job
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive

class ProtocolTest {
  private val bridge = ExampleSchemaProtocolBridge.create()

  @Test fun widgetVersionPropagated() = runTest {
    val composition = ProtocolRedwoodComposition(
      scope = this + BroadcastFrameClock(),
      bridge = bridge,
      changesSink = ::error,
      widgetVersion = 22U,
    )

    var actualDisplayVersion = 0U
    composition.setContent {
      actualDisplayVersion = WidgetVersion
    }
    composition.cancel()

    assertThat(actualDisplayVersion).isEqualTo(22U)
  }

  @Test fun protocolChangeOrder() = runTest {
    val composition = testProtocolComposition()

    composition.setContent {
      Row {
        Text("hey")
        Row {
          Text("hello")
        }
      }
    }

    assertThat(composition.awaitSnapshot()).isEqualTo(
      listOf(
        Create(Id(1), WidgetTag(1)), // Row
        ModifierChange(Id(1)),
        Create(Id(2), WidgetTag(3)), // Text
        ModifierChange(Id(2)),
        PropertyChange(Id(2), PropertyTag(1), JsonPrimitive("hey")), // text
        ChildrenChange.Add(Id(1), ChildrenTag(1), Id(2), 0),
        Create(Id(3), WidgetTag(1)), // Row
        ModifierChange(Id(3)),
        Create(Id(4), WidgetTag(3)), // Text
        ModifierChange(Id(4)),
        PropertyChange(Id(4), PropertyTag(1), JsonPrimitive("hello")), // text
        ChildrenChange.Add(Id(3), ChildrenTag(1), Id(4), 0),
        ChildrenChange.Add(Id(1), ChildrenTag(1), Id(3), 1),
        ChildrenChange.Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )
  }

  @Test fun protocolSkipsLambdaChangeOfSamePresence() = runTest {
    val composition = testProtocolComposition()

    var state by mutableStateOf(0)
    composition.setContent {
      Button(
        "state: $state",
        onClick = when (state) {
          0 -> { { state = 1 } }
          1 -> { { state = 2 } }
          2 -> { null }
          3 -> { null }
          else -> fail()
        },
      )
    }

    assertThat(composition.awaitSnapshot()).isEqualTo(
      listOf(
        Create(Id(1), WidgetTag(4)), // Button
        ModifierChange(Id(1)),
        PropertyChange(Id(1), PropertyTag(1), JsonPrimitive("state: 0")), // text
        PropertyChange(Id(1), PropertyTag(2), JsonPrimitive(true)), // onClick
        ChildrenChange.Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )

    // Invoke the onClick lambda to move the state from 0 to 1.
    bridge.sendEvent(Event(Id(1), EventTag(2)))

    assertThat(composition.awaitSnapshot()).isEqualTo(
      listOf(
        PropertyChange(Id(1), PropertyTag(1), JsonPrimitive("state: 1")), // text
      ),
    )

    // Invoke the onClick lambda to move the state from 1 to 2.
    bridge.sendEvent(Event(Id(1), EventTag(2)))

    assertThat(composition.awaitSnapshot()).isEqualTo(
      listOf(
        PropertyChange(Id(1), PropertyTag(1), JsonPrimitive("state: 2")), // text
        PropertyChange(Id(1), PropertyTag(2), JsonPrimitive(false)), // text
      ),
    )

    // Manually advance state from 2 to 3 to test null to null case.
    state = 3

    assertThat(composition.awaitSnapshot()).isEqualTo(
      listOf(
        PropertyChange(Id(1), PropertyTag(1), JsonPrimitive("state: 3")), // text
      ),
    )
  }

  private fun TestScope.testProtocolComposition(): TestRedwoodComposition<List<Change>> {
    val composition = TestRedwoodComposition(
      scope = backgroundScope,
      provider = bridge.provider,
      container = bridge.root,
    ) {
      bridge.getChangesOrNull() ?: emptyList()
    }
    backgroundScope.coroutineContext.job.invokeOnCompletion {
      composition.cancel()
    }
    return composition
  }
}
