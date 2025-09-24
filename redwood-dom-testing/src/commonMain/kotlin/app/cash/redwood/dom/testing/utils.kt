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

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

internal suspend fun HTMLCanvasElement.encodeImage(): Blob {
  return suspendCancellableCoroutine { continuation ->
    toBlob(
      _callback = { blob ->
        if (blob != null) {
          continuation.resume(blob)
        } else {
          continuation.resumeWithException(Exception("Failed to create blob"))
        }
      },
      type = "image/png",
    )
  }
}

internal suspend fun Blob.decodeImage(): HTMLImageElement {
  val url = URL.createObjectURL(this)
  try {
    return suspendCancellableCoroutine { continuation ->
      val img = document.createElement("img") as HTMLImageElement

      img.onload = { _ -> continuation.resume(img) }
      img.onerror = { _: dynamic, _: String, _: Int, _: Int, _: Any? ->
        continuation.resumeWithException(Exception("Failed to load image"))
      }
      img.src = url

      continuation.invokeOnCancellation {
        img.src = ""
      }
    }
  } finally {
    URL.revokeObjectURL(url)
  }
}
