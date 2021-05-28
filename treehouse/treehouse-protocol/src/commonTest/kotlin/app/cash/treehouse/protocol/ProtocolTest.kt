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
package app.cash.treehouse.protocol

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.test.Test
import kotlin.test.assertEquals

class ProtocolTest {
  private val format = Json {
    useArrayPolymorphism = true
    serializersModule = SerializersModule {
      polymorphic(Any::class) {
        subclass(String::class, String.serializer())
      }
    }
  }

  @Test fun eventNonNullValue() {
    val model = Event(1, 2, "Hello")
    val json = """{"id":1,"tag":2,"value":["kotlin.String","Hello"]}"""
    assertJsonRoundtrip(Event.serializer(), model, json)
  }

  @Test fun eventNullValue() {
    val model = Event(1, 2, null)
    val json = """{"id":1,"tag":2,"value":null}"""
    assertJsonRoundtrip(Event.serializer(), model, json)
  }

  @Test fun diff() {
    val model = Diff(
      childrenDiffs = listOf(
        ChildrenDiff.Clear,
        ChildrenDiff.Insert(1, 2, 3, 4, 5),
        ChildrenDiff.Move(1, 2, 3, 4, 5),
        ChildrenDiff.Remove(1, 2, 3, 4),
      ),
      propertyDiffs = listOf(
        PropertyDiff(1, 2, "Hello"),
        PropertyDiff(1, 2, null),
      ),
    )
    val json = "" +
      """{"childrenDiffs":[""" +
      """["app.cash.treehouse.protocol.ChildrenDiff.Clear",{}],""" +
      """["app.cash.treehouse.protocol.ChildrenDiff.Insert",{"id":1,"tag":2,"childId":3,"kind":4,"index":5}],""" +
      """["app.cash.treehouse.protocol.ChildrenDiff.Move",{"id":1,"tag":2,"fromIndex":3,"toIndex":4,"count":5}],""" +
      """["app.cash.treehouse.protocol.ChildrenDiff.Remove",{"id":1,"tag":2,"index":3,"count":4}]""" +
      """],"propertyDiffs":[""" +
      """{"id":1,"tag":2,"value":["kotlin.String","Hello"]},""" +
      """{"id":1,"tag":2,"value":null}""" +
      """]}"""
    assertJsonRoundtrip(Diff.serializer(), model, json)
  }

  private fun <T> assertJsonRoundtrip(serializer: KSerializer<T>, model: T, json: String) {
    assertEquals(json, format.encodeToString(serializer, model))
    assertEquals(model, format.decodeFromString(serializer, json))
  }
}
