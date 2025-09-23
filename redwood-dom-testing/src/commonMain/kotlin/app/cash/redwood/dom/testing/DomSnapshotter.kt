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

import kotlin.math.ceil
import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.files.Blob

/**
 * A image rendering of an HTML element.
 */
public data class DomSnapshot(
  public val image: Blob?,
  public val framedHtml: String,
)

public class DomSnapshotter {
  public suspend fun snapshot(
    element: Element,
    frame: Frame,
  ): DomSnapshot {
    require(element != document.documentElement && element.parentElement == null)

    // Wrap the element in a <div> with a 10px wide border. The div's border ensures the measurement
    // made by getBoundingClientRect() includes our element's margins.
    //
    // Note that later we have to subtract off the border size when we measure.
    val framingBorderSize = 10
    val wrapper = (document.createElement("div") as HTMLElement).apply {
      style.border = "${framingBorderSize}px solid red"
      style.width = frame.width?.let { "${it}px" } ?: "max-content"
      style.height = frame.height?.let { "${it}px" } ?: "max-content"
      style.display = "flex"
      style.flexDirection = "column"
      style.alignItems = "stretch"
    }
    wrapper.appendChild(element)
    document.documentElement!!.appendChild(wrapper)

    try {
      val boundingClientRect = wrapper.getBoundingClientRect()
      return DomSnapshot(
        image = HtmlToImage.toBlob(
          element = element,
          options = Options().apply {
            this.width = ceil(boundingClientRect.width).toInt() - (2 * framingBorderSize)
            this.height = ceil(boundingClientRect.height).toInt() - (2 * framingBorderSize)
            this.canvasWidth = this.width
            this.canvasHeight = this.height
            this.pixelRatio = frame.pixelRatio
          },
        ).await(),
        framedHtml = wrapper.outerHTML,
      )
    } finally {
      document.documentElement!!.removeChild(wrapper)
      wrapper.removeChild(element)
    }
  }
}
