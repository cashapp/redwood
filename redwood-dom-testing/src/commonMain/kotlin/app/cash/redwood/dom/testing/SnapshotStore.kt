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

import kotlin.js.Promise
import kotlinx.browser.window
import kotlinx.coroutines.await
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.w3c.fetch.RequestInit

internal class SnapshotStore {
  suspend fun put(fileName: String, data: ByteString) {
    val response = window.fetch(
      input = "/snapshots/$fileName",
      init = RequestInit(
        method = "POST",
        body = data.toByteArray(),
      ),
    ).await()

    if (!response.ok) {
      throw SnapshotStoreException(
        """
        |put $fileName failed: ${response.status} ${response.statusText}
        |${response.text().await()}
        """.trimMargin(),
      )
    }
  }

  suspend fun get(fileName: String): ByteString? {
    val response = window.fetch(
      input = "/snapshots/$fileName",
    ).await()

    if (response.status.toInt() == 404) {
      return null // No such file.
    }

    check(response.ok) {
      throw SnapshotStoreException(
        """
        |get $fileName failed: ${response.status} ${response.statusText}
        |${response.text().await()}
        """.trimMargin(),
      )
    }

    val bytes: Promise<ByteArray> = response.asDynamic().bytes()
    return bytes.await().toByteString()
  }
}

internal class SnapshotStoreException(message: String) : Exception(message)
