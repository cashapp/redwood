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
import org.w3c.dom.get
import org.w3c.files.Blob

/**
 * A image rendering of an HTML element.
 */
public data class DomSnapshot(
  public val images: List<Blob?>,
  public val framedHtml: String,
)

public class DomSnapshotter {
  public suspend fun snapshot(
    element: Element,
    frame: Frame,
    scrolling: Boolean,
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
      val elementWidth = ceil(boundingClientRect.width).toInt() - (2 * framingBorderSize)
      val elementHeight = ceil(boundingClientRect.height).toInt() - (2 * framingBorderSize)

      suspend fun captureImage(): Blob? {
        return html2canvas(
          element = element as HTMLElement,
          options = Options().apply {
            this.backgroundColor = null
            this.width = elementWidth
            this.height = elementHeight
            this.windowWidth = this.width
            this.windowHeight = this.height
            this.scale = frame.pixelRatio
          },
        ).await()?.encodeImage()
      }

      val images = buildList {
        if (!scrolling) {
          add(captureImage())
        } else {
          // Handle scrollable content.
          val scrollableElement = findScrollableElement(element)
            ?: throw IllegalStateException("No scrollable element found")

          // Calculate total number of pages needed.
          val totalPages =
            ceil(scrollableElement.scrollHeight.toDouble() / scrollableElement.clientHeight).toInt()

          for (page in 0 until totalPages) {
            scrollableElement.scrollTop = page * scrollableElement.clientHeight.toDouble()
            add(captureImage())
          }
        }
      }

      return DomSnapshot(
        images = images,
        framedHtml = wrapper.outerHTML,
      )
    } finally {
      document.documentElement!!.removeChild(wrapper)
      wrapper.removeChild(element)
    }
  }

  private fun findScrollableElement(element: Element): HTMLElement? {
    val elements = element.getElementsByTagName("div")
    for (i in 0 until elements.length) {
      val div = elements.get(i) as? HTMLElement ?: continue
      val style = kotlinx.browser.window.getComputedStyle(div)
      if (style.overflowY == "scroll" || style.overflowY == "auto") {
        if (div.scrollHeight > div.clientHeight) {
          return div
        }
      }
    }
    return null
  }
}
