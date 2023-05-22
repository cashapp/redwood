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
package app.cash.redwood.compose.testing

import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.isEqualTo
import example.redwood.compose.ExampleSchemaProtocolBridge
import example.redwood.compose.Row
import example.redwood.compose.Text
import example.redwood.widget.ExampleSchemaTester
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive

class ViewTreesTest {
  @Test fun nested() = runTest {
    val snapshot = ExampleSchemaTester {
      setContent {
        Row {
          Row {
            Text("One Fish")
            Text("Two Fish")
          }
          Row {
            Text("Red Fish")
            Text("Blue Fish")
          }
        }
      }
      awaitSnapshot()
    }

    val expected = listOf(
      Create(Id(1), WidgetTag(1)),
      Create(Id(2), WidgetTag(1)),
      Create(Id(3), WidgetTag(3)),
      ModifierChange(Id(3), emptyList()),
      PropertyChange(Id(3), PropertyTag(1), JsonPrimitive("One Fish")),
      Add(Id(2), ChildrenTag(1), Id(3), 0),
      Create(Id(4), WidgetTag(3)),
      ModifierChange(Id(4), emptyList()),
      PropertyChange(Id(4), PropertyTag(1), JsonPrimitive("Two Fish")),
      Add(Id(2), ChildrenTag(1), Id(4), 1),
      ModifierChange(Id(2), emptyList()),
      Add(Id(1), ChildrenTag(1), Id(2), 0),
      Create(Id(5), WidgetTag(1)),
      Create(Id(6), WidgetTag(3)),
      ModifierChange(Id(6), emptyList()),
      PropertyChange(Id(6), PropertyTag(1), JsonPrimitive("Red Fish")),
      Add(Id(5), ChildrenTag(1), Id(6), 0),
      Create(Id(7), WidgetTag(3)),
      ModifierChange(Id(7), emptyList()),
      PropertyChange(Id(7), PropertyTag(1), JsonPrimitive("Blue Fish")),
      Add(Id(5), ChildrenTag(1), Id(7), 1),
      ModifierChange(Id(5), emptyList()),
      Add(Id(1), ChildrenTag(1), Id(5), 1),
      ModifierChange(Id(1), emptyList()),
      Add(Id.Root, ChildrenTag.Root, Id(1), 0),
    )

    assertThat(snapshot.toViewTree(ExampleSchemaProtocolBridge).changes)
      .isEqualTo(expected)
    assertThat(snapshot.single().toViewTree(ExampleSchemaProtocolBridge).changes)
      .isEqualTo(expected)
  }
}
