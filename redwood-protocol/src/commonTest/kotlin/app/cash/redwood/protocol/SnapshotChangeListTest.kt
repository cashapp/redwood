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
package app.cash.redwood.protocol

import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenChange.Move
import app.cash.redwood.protocol.ChildrenChange.Remove
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

class SnapshotChangeListTest {
  @Test fun validChangesSerialization() {
    val expected = SnapshotChangeList(
      listOf(
        Create(Id(1), WidgetTag(1)),
        ModifierChange(Id(1), emptyList()),
        PropertyChange(Id(1), WidgetTag(1), PropertyTag(1), JsonPrimitive("Hello")),
        Add(Id.Root, ChildrenTag.Root, Id(1), 0),
      ),
    )

    val json = Json.encodeToString(SnapshotChangeList.serializer(), expected)
    assertThat(json).isEqualTo(
      """
      |[
      |{"type":"create","id":1,"tag":1},
      |{"type":"modifier","id":1},
      |{"type":"property","id":1,"widget":1,"tag":1,"value":"Hello"},
      |{"type":"add","id":0,"tag":1,"childId":1,"index":0}
      |]
      """.trimMargin().replace("\n", ""),
    )

    val actual = Json.decodeFromString(SnapshotChangeList.serializer(), json)
    assertThat(actual).isEqualTo(expected)
  }

  @Test fun invalidChanges() {
    assertFailure {
      SnapshotChangeList(
        listOf(
          Create(Id(1), WidgetTag(1)),
          ModifierChange(Id(1), emptyList()),
          Move(Id.Root, ChildrenTag.Root, 1, 2, 3),
          PropertyChange(Id(1), WidgetTag(1), PropertyTag(1), JsonPrimitive("Hello")),
          Add(Id.Root, ChildrenTag.Root, Id(1), 0),
          Remove(Id.Root, ChildrenTag.Root, 1, 2),
        ),
      )
    }.hasMessage(
      """
      |Snapshot change list cannot contain move or remove operations
      |
      |Found:
      | - Move(_id=0, _tag=1, fromIndex=1, toIndex=2, count=3)
      | - Remove(_id=0, _tag=1, index=1, count=2)
      """.trimMargin(),
    )
  }
}
