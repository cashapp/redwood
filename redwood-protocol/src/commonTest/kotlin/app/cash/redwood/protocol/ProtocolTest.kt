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
package app.cash.redwood.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

class ProtocolTest {
  private val format = Json {
    useArrayPolymorphism = true
  }

  @Test fun eventNonNullValue() {
    val model = Event(Id(1), EventTag(2), JsonPrimitive("Hello"))
    val json = """{"id":1,"tag":2,"value":"Hello"}"""
    assertJsonRoundtrip(Event.serializer(), model, json)
  }

  @Test fun eventNullValue() {
    val model = Event(Id(1), EventTag(2))
    val json = """{"id":1,"tag":2}"""
    assertJsonRoundtrip(Event.serializer(), model, json)
  }

  @Test fun diff() {
    val model = Diff(
      childrenDiffs = listOf(
        ChildrenDiff.Insert(Id(1), 2U, Id(3), 4, 5),
        ChildrenDiff.Move(Id(1), 2U, 3, 4, 5),
        ChildrenDiff.Remove(Id(1), 2U, 3, 4),
      ),
      layoutModifiers = listOf(
        LayoutModifiers(
          Id(1),
          buildJsonArray {
            add(
              buildJsonArray {
                add(JsonPrimitive(1))
                add(buildJsonObject { })
              },
            )
          },
        ),
      ),
      propertyDiffs = listOf(
        PropertyDiff(Id(1), 2U, JsonPrimitive("Hello")),
        PropertyDiff(Id(1), 2U, JsonNull),
      ),
    )
    val json = "" +
      """{"childrenDiffs":[""" +
      """["insert",{"id":1,"tag":2,"childId":3,"kind":4,"index":5}],""" +
      """["move",{"id":1,"tag":2,"fromIndex":3,"toIndex":4,"count":5}],""" +
      """["remove",{"id":1,"tag":2,"index":3,"count":4}]""" +
      """],"layoutModifiers":[""" +
      """{"id":1,"elements":[[1,{}]]}""" +
      """],"propertyDiffs":[""" +
      """{"id":1,"tag":2,"value":"Hello"},""" +
      """{"id":1,"tag":2}""" +
      """]}"""
    assertJsonRoundtrip(Diff.serializer(), model, json)
  }

  @Test fun diffEmptyLists() {
    val model = Diff(
      childrenDiffs = listOf(),
      layoutModifiers = listOf(),
      propertyDiffs = listOf(),
    )
    val json = "{}"
    assertJsonRoundtrip(Diff.serializer(), model, json)
  }

  private fun <T> assertJsonRoundtrip(serializer: KSerializer<T>, model: T, json: String) {
    assertEquals(json, format.encodeToString(serializer, model))
    assertEquals(model, format.decodeFromString(serializer, json))
  }
}
