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

    var differentPixels = 0L
    var deltaRGB = 0L
    var deltaA = 0L

    // Compare pixels
    for (y in 0 until maxHeight) {
      for (x in 0 until maxWidth) {
        val i = (y * maxWidth + x) * 4

        // Check if pixel exists in image
        val hasExpected = x < expectedWidth && y < expectedHeight
        val hasActual = x < actualWidth && y < actualHeight

        // Skip if neither image has a pixel at this location
        if (!hasExpected && !hasActual) {
          continue
        }

        val expectedR = if (hasExpected) expectedData.data[i].toInt() else 0
        val expectedG = if (hasExpected) expectedData.data[i + 1].toInt() else 0
        val expectedB = if (hasExpected) expectedData.data[i + 2].toInt() else 0
        val expectedA = if (hasExpected) expectedData.data[i + 3].toInt() else 0

        val actualR = if (hasActual) actualData.data[i].toInt() else 0
        val actualG = if (hasActual) actualData.data[i + 1].toInt() else 0
        val actualB = if (hasActual) actualData.data[i + 2].toInt() else 0
        val actualA = if (hasActual) actualData.data[i + 3].toInt() else 0

        // If pixels are identical, make it transparent
        if (hasExpected && hasActual && actualR == expectedR && actualG == expectedG && actualB == expectedB && actualA == expectedA) {
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

        // For missing pixels, treat as maximum difference
        if (!hasExpected || !hasActual) {
          deltaRGB += 255L * 3  // Maximum RGB difference
          deltaA += 255L        // Maximum alpha difference
        } else {
          // For actual pixel differences, use real deltas
          deltaRGB += abs(actualR - expectedR).toLong()
          deltaRGB += abs(actualG - expectedG).toLong()
          deltaRGB += abs(actualB - expectedB).toLong()
          deltaA += abs(actualA - expectedA).toLong()
        }
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
