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
package app.cash.redwood.treehouse

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class StateSnapshotTest {

  @Test
  fun toValueMapWorksAsExpected() {
    val stateSnapshot = stateSnapshot()
    val valuesMap = stateSnapshot.toValuesMap()
    assertThat(valuesMap.entries.size).isEqualTo(5)
    assertTrue(valuesMap["key1"]!![0] is MutableState<*>)
    assertThat((valuesMap["key1"]!![0] as MutableState<*>).value).isEqualTo(1.0)

    assertThat(valuesMap["key2"]).isEqualTo(listOf(1.0))

    assertThat(valuesMap["key3"]!![0] is MutableState<*>)
    assertThat((valuesMap["key3"]!![0] as MutableState<*>).value).isEqualTo("str")

    assertThat(valuesMap["key4"]).isEqualTo(listOf("str"))

    assertThat(valuesMap["key5"]).isEqualTo(listOf(null))
  }

  @Test
  fun toStateSnapshotWorksAsExpected() {
    val storedStateSnapshot = storedStateSnapshot()
    val stateSnapshot = storedStateSnapshot.toStateSnapshot()
    assertThat(stateSnapshot.content).containsOnly(
      "key1" to listOf(JsonMutableState(JsonPrimitive(1))),
      "key2" to listOf(JsonPrimitive(1)),
      "key3" to listOf(JsonMutableState(JsonPrimitive("str"))),
      "key4" to listOf(JsonPrimitive("str")),
      "key5" to listOf(JsonNull),
    )
  }

  private fun stateSnapshot() = StateSnapshot(
    mapOf(
      "key1" to listOf(JsonMutableState(JsonPrimitive(1))),
      "key2" to listOf(JsonPrimitive(1)),
      "key3" to listOf(JsonMutableState(JsonPrimitive("str"))),
      "key4" to listOf(JsonPrimitive("str")),
      "key5" to listOf(JsonNull),
    ),
  )

  private fun storedStateSnapshot() = mapOf(
    "key1" to listOf(mutableStateOf(1)),
    "key2" to listOf(1),
    "key3" to listOf(mutableStateOf("str")),
    "key4" to listOf("str"),
    "key5" to listOf(null),
  )
}
