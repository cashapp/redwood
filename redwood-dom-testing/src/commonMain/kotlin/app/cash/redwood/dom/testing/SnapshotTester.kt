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

import org.w3c.dom.Element
import org.w3c.files.Blob

public class SnapshotTester @PublishedApi internal constructor(
  private val snapshotStore: SnapshotStore = SnapshotStore(),
  private val domSnapshotter: DomSnapshotter = DomSnapshotter(),
  private val imageDiffer: ImageDiffer = ImageDiffer(),
  private val path: String,
) {

  public suspend fun snapshot(
    element: Element,
    name: String = "snapshot",
    frame: Frame,
    scrolling: Boolean = false,
  ) {
    val (image, html) = domSnapshotter.snapshot(element, frame)

    val fileName = "$path/$name.png"

    if (image == null) {
      snapshotStore.put("$path/$name.actual.html", Blob(arrayOf(html)), writeToBuildDir = true)
      throw SnapshotMismatchException("HtmlToImage.toBlob returned null for $fileName")
    }

    val existing = snapshotStore.getBlob(fileName)
    if (existing == null) {
      snapshotStore.put(fileName, image)
      return
    }

    val diffResult = imageDiffer.compare(existing, image)
    if (!diffResult.isDifferent) return

    // Save the delta image and wrapped HTML so the developer can see what's different.
    snapshotStore.put("$path/$name.diff.png", diffResult.deltaImage!!, writeToBuildDir = true)
    snapshotStore.put("$path/$name.actual.html", Blob(arrayOf(html)), writeToBuildDir = true)

    throw SnapshotMismatchException(
      "Current snapshot does not match the existing file $fileName " +
        "(${diffResult.percentDifference}% different, ${diffResult.numDifferentPixels} pixels)",
    )
  }

  public companion object Companion {
    public operator fun invoke(path: String): SnapshotTester =
      SnapshotTester(path = path)
  }
}
