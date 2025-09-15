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
import org.w3c.fetch.Response
import org.w3c.files.Blob

internal class SnapshotStore {
  suspend fun put(fileName: String, data: ByteString, writeToBuildDir: Boolean = false) {
    putInternal(fileName, data.toByteArray(), writeToBuildDir)
  }

  suspend fun put(fileName: String, data: Blob, writeToBuildDir: Boolean = false) {
    putInternal(fileName, data, writeToBuildDir)
  }

  private suspend fun putInternal(fileName: String, data: dynamic, writeToBuildDir: Boolean) {
    val url = if (writeToBuildDir) {
      "/snapshots/$fileName?dir=build"
    } else {
      "/snapshots/$fileName"
    }

    val response = window.fetch(
      input = url,
      init = RequestInit(
        method = "POST",
        body = data,
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

  suspend fun getBlob(fileName: String): Blob? {
    return getInternal(fileName)?.blob()?.await()
  }

  suspend fun getByteString(fileName: String): ByteString? {
    val response = getInternal(fileName) ?: return null
    val bytes: Promise<ByteArray> = response.asDynamic().bytes()
    return bytes.await().toByteString()
  }

  private suspend fun getInternal(fileName: String): Response? {
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
    return response
  }
}

internal class SnapshotStoreException(message: String) : Exception(message)
