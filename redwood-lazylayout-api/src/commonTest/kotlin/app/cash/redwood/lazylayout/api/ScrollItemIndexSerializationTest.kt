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
package app.cash.redwood.lazylayout.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.json.Json

class ScrollItemIndexSerializationTest {
  private val json = Json {
    ignoreUnknownKeys = true
    useArrayPolymorphism = true
  }

  @Test
  fun encodeAndDecode() {
    assertRoundTrip(
      ScrollItemIndex(3, 7),
      """{"id":3,"index":7}""",
    )
  }

  private fun assertRoundTrip(value: ScrollItemIndex, encoded: String) {
    assertThat(json.encodeToString(ScrollItemIndex.serializer(), value)).isEqualTo(encoded)
    assertThat(json.decodeFromString(ScrollItemIndex.serializer(), encoded)).isEqualTo(value)
  }
}
