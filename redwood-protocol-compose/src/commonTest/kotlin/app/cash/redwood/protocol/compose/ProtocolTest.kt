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
import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifiers
import app.cash.redwood.protocol.PropertyDiff
import example.redwood.compose.Button
import example.redwood.compose.DiffProducingExampleSchemaWidgetFactory
import example.redwood.compose.Row
import example.redwood.compose.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

@OptIn(ExperimentalCoroutinesApi::class)
class ProtocolTest {
  @Test fun widgetVersionPropagated() = runTest {
    val clock = BroadcastFrameClock()
    val composition = ProtocolRedwoodComposition(
      scope = this + clock,
      factory = DiffProducingExampleSchemaWidgetFactory(ProtocolBridge()),
      diffSink = ::error,
      widgetVersion = 22U,
    )

    var actualDisplayVersion = 0U
    composition.setContent {
      actualDisplayVersion = WidgetVersion
    }
    composition.cancel()

    assertEquals(22U, actualDisplayVersion)
  }

  @Test fun childrenInheritIdFromSyntheticParent() = runTest {
    val clock = BroadcastFrameClock()
    val diffs = ArrayDeque<Diff>()
    val composition = ProtocolRedwoodComposition(
      scope = this + clock,
      factory = DiffProducingExampleSchemaWidgetFactory(ProtocolBridge()),
      diffSink = { diff -> diffs += diff },
      widgetVersion = 1U,
    )

    composition.setContent {
      Row {
        Text("hey")
        Row {
          Text("hello")
        }
      }
    }

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Insert(Id.Root, RootChildrenTag, Id(1U), 1 /* row */, 0),
          ChildrenDiff.Insert(Id(1U), 1U, Id(2U), 3 /* text */, 0),
          ChildrenDiff.Insert(Id(1U), 1U, Id(3U), 1 /* row */, 1),
          ChildrenDiff.Insert(Id(3U), 1U, Id(4U), 3 /* text */, 0),
        ),
        layoutModifiers = listOf(
          LayoutModifiers(Id(1U), JsonArray(listOf())),
          LayoutModifiers(Id(2U), JsonArray(listOf())),
          LayoutModifiers(Id(3U), JsonArray(listOf())),
          LayoutModifiers(Id(4U), JsonArray(listOf())),
        ),
        propertyDiffs = listOf(
          PropertyDiff(Id(2U), 1U /* text */, JsonPrimitive("hey")),
          PropertyDiff(Id(4U), 1U /* text */, JsonPrimitive("hello")),
        ),
      ),
      diffs.removeFirst(),
    )

    composition.cancel()
  }

  @Test fun protocolSkipsLambdaChangeOfSamePresence() = runTest {
    val clock = BroadcastFrameClock()
    var state by mutableStateOf(0)
    val bridge = ProtocolBridge()
    val diffs = ArrayDeque<Diff>()
    val composition = ProtocolRedwoodComposition(
      scope = this + clock,
      factory = DiffProducingExampleSchemaWidgetFactory(bridge),
      diffSink = { diff -> diffs += diff },
      widgetVersion = 1U,
    )

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

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Insert(Id.Root, RootChildrenTag, Id(1U), 4 /* button */, 0),
        ),
        layoutModifiers = listOf(
          LayoutModifiers(Id(1U), JsonArray(listOf())),
        ),
        propertyDiffs = listOf(
          PropertyDiff(Id(1U), 1U /* text */, JsonPrimitive("state: 0")),
          PropertyDiff(Id(1U), 2U /* onClick */, JsonPrimitive(true)),
        ),
      ),
      diffs.removeFirst(),
    )

    // Invoke the onClick lambda to move the state from 0 to 1.
    bridge.sendEvent(Event(Id(1U), 2U))
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        propertyDiffs = listOf(
          PropertyDiff(Id(1U), 1U /* text */, JsonPrimitive("state: 1")),
        ),
      ),
      diffs.removeFirst(),
    )

    // Invoke the onClick lambda to move the state from 1 to 2.
    bridge.sendEvent(Event(Id(1U), 2U))
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        propertyDiffs = listOf(
          PropertyDiff(Id(1U), 1U /* text */, JsonPrimitive("state: 2")),
          PropertyDiff(Id(1U), 2U /* text */, JsonPrimitive(false)),
        ),
      ),
      diffs.removeFirst(),
    )

    // Manually advance state from 2 to 3 to test null to null case.
    state = 3
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        propertyDiffs = listOf(
          PropertyDiff(Id(1U), 1U /* text */, JsonPrimitive("state: 3")),
        ),
      ),
      diffs.removeFirst(),
    )

    composition.cancel()
  }
}
