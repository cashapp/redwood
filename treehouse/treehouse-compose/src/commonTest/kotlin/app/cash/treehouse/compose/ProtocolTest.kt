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
package app.cash.treehouse.compose

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.cash.treehouse.protocol.ChildrenDiff
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootId
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff
import example.treehouse.compose.Box
import example.treehouse.compose.Button
import example.treehouse.compose.Text
import kotlinx.coroutines.plus
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProtocolTest {
  @Test fun childrenInheritIdFromSyntheticParent() = runTest {
    val clock = BroadcastFrameClock()
    val composition = TreehouseComposition(scope = this + clock)
    val diffs = ArrayDeque<Diff>()
    composition.start { diff -> diffs += diff }

    composition.setContent {
      Box {
        Text("hey")
        Box {
          Text("hello")
        }
      }
    }

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Insert(RootId, RootChildrenTag, 1L, 1 /* box */, 0),
          ChildrenDiff.Insert(1L, 1, 2L, 2 /* text */, 0),
          ChildrenDiff.Insert(1L, 1, 3L, 1 /* box */, 1),
          ChildrenDiff.Insert(3L, 1, 4L, 2 /* text */, 0),
        ),
        propertyDiffs = listOf(
          PropertyDiff(2L, 1 /* text */, "hey".json),
          PropertyDiff(4L, 1 /* text */, "hello".json),
        ),
      ),
      diffs.removeFirst()
    )

    composition.cancel()
  }

  @Test fun protocolSkipsLambdaChangeOfSamePresence() = runTest {
    val clock = BroadcastFrameClock()
    var state by mutableStateOf(0)
    val composition = TreehouseComposition(scope = this + clock)
    val diffs = ArrayDeque<Diff>()
    composition.start { diff -> diffs += diff }

    composition.setContent {
      Button(
        "state: $state",
        onClick = when (state) {
          0 -> { { state = 1 } }
          1 -> { { state = 2 } }
          2 -> { null }
          3 -> { null }
          else -> fail()
        }
      )
    }

    clock.awaitFrame()
    assertEquals(
      Diff(
        childrenDiffs = listOf(
          ChildrenDiff.Insert(RootId, RootChildrenTag, 1L, 3 /* button */, 0),
        ),
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 0".json),
          PropertyDiff(1L, 2 /* onClick */, true.json),
        ),
      ),
      diffs.removeFirst()
    )

    // Invoke the onClick lambda to move the state from 0 to 1.
    composition.sendEvent(Event(1L, 2, null))
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 1".json),
        ),
      ),
      diffs.removeFirst()
    )

    // Invoke the onClick lambda to move the state from 1 to 2.
    composition.sendEvent(Event(1L, 2, null))
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 2".json),
          PropertyDiff(1L, 2 /* text */, false.json),
        ),
      ),
      diffs.removeFirst()
    )

    // Manually advance state from 2 to 3 to test null to null case.
    state = 3
    yield() // Allow state change to be handled.

    clock.awaitFrame()
    assertEquals(
      Diff(
        propertyDiffs = listOf(
          PropertyDiff(1L, 1 /* text */, "state: 3".json),
        ),
      ),
      diffs.removeFirst()
    )

    composition.cancel()
  }
}
