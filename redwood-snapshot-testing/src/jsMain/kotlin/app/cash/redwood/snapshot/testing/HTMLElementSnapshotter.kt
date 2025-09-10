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
package app.cash.redwood.snapshot.testing

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.redwood.dom.testing.DomSnapshotter
import app.cash.redwood.dom.testing.Frame
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

class HTMLElementSnapshotter(
  private val testFunction: CoroutineTestFunction,
  private val frame: Frame,
  private val widget: HTMLElement,
) : Snapshotter {
  override suspend fun snapshot(name: String?, scrolling: Boolean) {
    val domSnapshotter = DomSnapshotter(
      path = buildString {
        if (testFunction.packageName != "") {
          append(testFunction.packageName)
          append("/")
        }
        append(testFunction.className)
      },
    )
    domSnapshotter.snapshot(
      element = widget,
      name = buildString {
        append(testFunction.functionName)
        if (name != null) {
          append("_")
          append(name)
        }
      },
      frame = frame,
      scrolling = scrolling,
    )
  }

  class Factory(
    val frame: Frame,
  ) : Snapshotter.Factory<HTMLElement> {
    private var testFunction: CoroutineTestFunction? = null

    override fun invoke(widget: HTMLElement): Snapshotter {
      val testFunction = testFunction

      // Wrap the element to get a white background.
      // Use flexbox to match the wrapper's size to the content size.
      val backgroundWrapper = (document.createElement("div") as HTMLDivElement).apply {
        style.backgroundColor = "white"
        style.display = "flex"
        style.flexDirection = "column"
        style.alignItems = "stretch"
      }
      backgroundWrapper.appendChild(widget)

      return HTMLElementSnapshotter(
        testFunction = testFunction ?: error("unexpected invoke() without running test"),
        frame = frame,
        widget = backgroundWrapper,
      )
    }

    override suspend fun intercept(testFunction: CoroutineTestFunction) {
      if (this.testFunction != null) {
        testFunction()
        return
      }

      this.testFunction = testFunction
      try {
        testFunction()
      } finally {
        this.testFunction = null
      }
    }
  }
}
