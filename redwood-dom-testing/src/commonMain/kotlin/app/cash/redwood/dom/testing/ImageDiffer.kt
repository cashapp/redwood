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

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlinx.browser.document
import org.khronos.webgl.get
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.files.Blob

public data class DiffResult(
  val isDifferent: Boolean,
  val deltaImage: Blob? = null,
  val percentDifference: Float = 0f,
  val numDifferentPixels: Long = 0,
)

internal class ImageDiffer {
  suspend fun compare(expected: Blob, actual: Blob): DiffResult {
    val expectedImage = expected.decodeImage()
    val expectedWidth = expectedImage.width
    val expectedHeight = expectedImage.height

    val actualImage = actual.decodeImage()
    val actualWidth = actualImage.width
    val actualHeight = actualImage.height

    val maxWidth = max(expectedWidth, actualWidth)
    val maxHeight = max(expectedHeight, actualHeight)
    val minWidth = min(expectedWidth, actualWidth)
    val minHeight = min(expectedHeight, actualHeight)

    // Create canvas for composite image (expected + delta + actual)
    val canvas = document.createElement("canvas") as HTMLCanvasElement
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    canvas.width = maxWidth * 3 // Three sections of maxWidth
    canvas.height = maxHeight

    // Draw expected image on the left
    ctx.drawImage(expectedImage, 0.0, 0.0)
    val expectedData = ctx.getImageData(0.0, 0.0, maxWidth.toDouble(), maxHeight.toDouble())

    // Draw actual image on the right
    ctx.drawImage(actualImage, maxWidth * 2.0, 0.0)
    val actualData =
      ctx.getImageData(maxWidth * 2.0, 0.0, maxWidth.toDouble(), maxHeight.toDouble())

    // Create delta image data
    val deltaData = ctx.createImageData(maxWidth.toDouble(), maxHeight.toDouble())
    val deltaArray = deltaData.data.asDynamic()

    var differentPixels = (maxWidth.toLong() * maxHeight) - (minWidth.toLong() * minHeight)
    var deltaRGB = 0L
    var deltaA = (differentPixels * 255)

    // Compare pixels
    for (y in 0 until minWidth) {
      for (x in 0 until minHeight) {
        val i = (y * maxWidth + x) * 4

        val expectedR = expectedData.data[i].toInt()
        val expectedG = expectedData.data[i + 1].toInt()
        val expectedB = expectedData.data[i + 2].toInt()
        val expectedA = expectedData.data[i + 3].toInt()

        val actualR = actualData.data[i].toInt()
        val actualG = actualData.data[i + 1].toInt()
        val actualB = actualData.data[i + 2].toInt()
        val actualA = actualData.data[i + 3].toInt()

        // If pixels are identical, make it transparent
        if (actualR == expectedR && actualG == expectedG && actualB == expectedB && actualA == expectedA) {
          deltaArray[i] = expectedR
          deltaArray[i + 1] = expectedG
          deltaArray[i + 2] = expectedB
          deltaArray[i + 3] = min(expectedA, 32)
          continue
        }

        differentPixels++

        // Visualize differences with red pixel
        deltaArray[i] = 255
        deltaArray[i + 1] = 0
        deltaArray[i + 2] = 0
        deltaArray[i + 3] = 255

        deltaRGB += abs(actualR - expectedR).toLong()
        deltaRGB += abs(actualG - expectedG).toLong()
        deltaRGB += abs(actualB - expectedB).toLong()
        deltaA += abs(actualA - expectedA).toLong()
      }
    }

    if (differentPixels == 0L) {
      return DiffResult(isDifferent = false)
    }

    // Draw delta image in the middle
    ctx.putImageData(deltaData, maxWidth.toDouble(), 0.0)

    // Calculate percentage difference
    val totalPixels = maxHeight.toLong() * maxWidth.toLong()
    val percentDifference =
      (deltaRGB * 100 / (totalPixels * 3L * 255L).toDouble()).toFloat().takeIf { it != 0f }
        ?: (deltaA * 100 / (totalPixels * 255L).toDouble()).toFloat()

    return DiffResult(
      isDifferent = true,
      deltaImage = canvas.encodeImage(),
      percentDifference = percentDifference,
      numDifferentPixels = differentPixels,
    )
  }
}
