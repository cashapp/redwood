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
import assertk.assertions.corresponds
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import kotlin.test.Test
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StateSnapshotTest {
  @Test
  fun stateSnapshotSerializeThenDeserialize() {
    val json = Json {
      prettyPrint = true
      useArrayPolymorphism = true
      serializersModule = SaveableStateSerializersModule
    }
    val stateSnapshot = StateSnapshot(
      mapOf(
        "key1" to listOf(mutableStateOf(1)),
        "key2" to listOf(1),
        "key3" to listOf(mutableStateOf("str")),
        "key4" to listOf("str"),
        "key5" to listOf(null),
        "key6" to listOf(true),
        "key7" to listOf(1.5),
        "key8" to listOf(listOf(1, "str")),
      ),
    )
    val serialized = json.encodeToString(stateSnapshot)
    val deserialized = json.decodeFromString<StateSnapshot>(serialized)

    assertThat(serialized).isEqualTo(
      """
      {
          "content": {
              "key1": [
                  ["MutableState", {
                          "value": ["kotlin.Int", 1
                          ]
                      }
                  ]
              ],
              "key2": [
                  ["kotlin.Int", 1
                  ]
              ],
              "key3": [
                  ["MutableState", {
                          "value": ["kotlin.String", "str"
                          ]
                      }
                  ]
              ],
              "key4": [
                  ["kotlin.String", "str"
                  ]
              ],
              "key5": [
                  null
              ],
              "key6": [
                  ["kotlin.Boolean", true
                  ]
              ],
              "key7": [
                  ["kotlin.Double", 1.5
                  ]
              ],
              "key8": [
                  ["kotlin.collections.ArrayList", [
                          ["kotlin.Int", 1
                          ],
                          ["kotlin.String", "str"
                          ]
                      ]
                  ]
              ]
          }
      }
      """.trimIndent(),
    )
    assertThat(deserialized.content).hasSize(stateSnapshot.content.size)
    assertThat(deserialized.content["key1"]!![0])
      .isNotNull()
      .isInstanceOf<MutableState<*>>()
      .corresponds(mutableStateOf(1), ::mutableStateCorrespondence)
    assertThat(deserialized.content["key2"]).isEqualTo(listOf(1))
    assertThat(deserialized.content["key3"]!![0])
      .isNotNull()
      .isInstanceOf<MutableState<*>>()
      .corresponds(mutableStateOf("str"), ::mutableStateCorrespondence)
    assertThat(deserialized.content["key4"]).isEqualTo(listOf("str"))
    assertThat(deserialized.content["key5"]).isEqualTo(listOf(null))
    assertThat(deserialized.content["key6"]).isEqualTo(listOf(true))
    assertThat(deserialized.content["key7"]).isEqualTo(listOf(1.5))
    assertThat(deserialized.content["key8"]).isEqualTo(listOf(listOf(1, "str")))
  }
}

private fun mutableStateCorrespondence(
  actual: MutableState<*>,
  expected: MutableState<*>,
): Boolean {
  return actual.value == expected.value
}
