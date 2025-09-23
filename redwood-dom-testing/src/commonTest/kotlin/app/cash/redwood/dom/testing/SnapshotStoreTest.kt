/*
 * Copyright (C) 2025 Square, Inc.
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
package app.cash.redwood.dom.testing

import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

internal class SnapshotStoreTest {
  private val path = "app.cash.redwood.dom.testing/SnapshotStoreTest"

  @Test
  fun putAndGetFile() = runTest {
    val store = SnapshotStore()
    val data = "Hello World!".encodeUtf8()
    store.put("$path/putAndGetFile.txt", data)
    assertThat(store.getByteString("$path/putAndGetFile.txt")).isEqualTo(data)
  }

  @Test
  fun getDirectoryTraversalReturnsNoData() = runTest {
    val store = SnapshotStore()
    // 404 Not Found.
    assertThat(store.getByteString("../README.md")).isNull()
    assertThat(store.getByteString("../../README.md")).isNull()
    assertThat(store.getByteString("../../../README.md")).isNull()
    assertThat(store.getByteString("../../../../README.md")).isNull()
  }

  @Test
  fun putDirectoryTraversalRejected() = runTest {
    val store = SnapshotStore()
    val data = "Hacks!".encodeUtf8()
    val exception = assertFailsWith<SnapshotStoreException> {
      store.put("../hello.txt", data)
    }
    assertThat(exception).hasMessage(
      """
      |put ../hello.txt failed: 404 Not Found
      |NOT FOUND
      """.trimMargin(),
    )
  }
}
