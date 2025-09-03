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
import kotlin.math.ceil
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob

public class DomSnapshotter @PublishedApi internal constructor(
  private val path: String,
) {
  private val snapshotStore = SnapshotStore()

  public suspend fun snapshot(
    element: Element,
    name: String = "snapshot",
    scrolling: Boolean = false,
    width: Int? = null,
    height: Int? = null,
  ) {
    element.setAttribute(
      "style",
      "width: ${width?.let { "${it}px" } ?: "max-content"}; " +
        "height: ${height?.let { "${it}px" } ?: "max-content"};",
    )

    val image = HtmlToImage.toBlob(
      element = element,
      options = Options().apply {
        this.backgroundColor = "#ffff66"
        this.width = ceil(element.getBoundingClientRect().width).toInt()
        this.height = ceil(element.getBoundingClientRect().height).toInt()
        this.canvasWidth = this.width
        this.canvasHeight = this.height
        this.pixelRatio = 1.0
      },
    ).await()

    val fileName = "$path/$name.png"

    snapshotStore.getBlob(fileName)?.let { existing ->
      check(existing.contentEquals(image)) {
        "Current snapshot does not match the existing file $fileName"
      }
    } ?: snapshotStore.put(fileName, image)
  }

  private suspend fun Blob.contentEquals(other: Blob): Boolean {
    if (this.size != other.size) return false

    val url1 = URL.createObjectURL(this)
    val url2 = URL.createObjectURL(other)

    try {
      val img1 = loadImage(url1)
      val img2 = loadImage(url2)

      if (img1.width != img2.width || img1.height != img2.height) {
        return false
      }

      val canvas = document.createElement("canvas") as HTMLCanvasElement
      val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

      canvas.width = img1.width
      canvas.height = img1.height

      // Get data for first image
      ctx.drawImage(img1, 0.0, 0.0)
      val data1 = ctx.getImageData(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

      // Get data for second image
      ctx.clearRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
      ctx.drawImage(img2, 0.0, 0.0)
      val data2 = ctx.getImageData(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

      // Compare pixel by pixel
      val pixels1 = data1.data
      val pixels2 = data2.data
      for (i in 0 until pixels1.length) {
        if (pixels1[i] != pixels2[i]) {
          return false
        }
      }

      return true
    } finally {
      URL.revokeObjectURL(url1)
      URL.revokeObjectURL(url2)
    }
  }

  private suspend fun loadImage(url: String): HTMLImageElement =
    suspendCancellableCoroutine { continuation ->
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

  public companion object Companion {
    public inline operator fun invoke(): DomSnapshotter {
      return DomSnapshotter("PlaceholderTestName")
    }
  }
}
