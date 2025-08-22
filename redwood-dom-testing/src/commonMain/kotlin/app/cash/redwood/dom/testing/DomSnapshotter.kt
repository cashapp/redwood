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

import kotlinx.coroutines.await
import org.w3c.dom.Element

public class DomSnapshotter @PublishedApi internal constructor(
  private val path: String,
) {
  private val snapshotStore = SnapshotStore()

  public suspend fun snapshot(
    element: Element,
    name: String = "snapshot",
    scrolling: Boolean = false,
  ) {
    val image = HtmlToImage.toBlob(
      element = element,
      options = Options().apply {
        this.backgroundColor = "#ffff66"
        this.width = 300
        this.height = 100
        this.canvasWidth = width
        this.canvasHeight = height
        this.pixelRatio = 3.0
      },
    ).await()

    snapshotStore.put("$path/${name ?: "snapshot"}.png", image)
  }

  public companion object Companion {
    public inline operator fun invoke(): DomSnapshotter {
      return DomSnapshotter("PlaceholderTestName")
    }
  }
}
