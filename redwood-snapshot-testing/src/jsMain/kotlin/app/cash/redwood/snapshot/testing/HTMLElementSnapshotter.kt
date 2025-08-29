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

import app.cash.burst.TestFunction
import app.cash.redwood.dom.testing.DomSnapshotter
import org.w3c.dom.HTMLElement

class HTMLElementSnapshotter(
  private val testFunction: TestFunction,
  private val widget: HTMLElement,
) : Snapshotter {
  override suspend fun snapshot(name: String?, scrolling: Boolean) {
    println("SNAPSHOTTING $name $testFunction")

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
      scrolling = scrolling,
    )
  }

  class Factory : Snapshotter.Factory<HTMLElement> {
    private var testFunction: TestFunction? = null

    override fun invoke(widget: HTMLElement): Snapshotter {
      val testFunction = testFunction

      return HTMLElementSnapshotter(
        testFunction = testFunction ?: error("unexpected invoke() without running test"),
        widget = widget,
      )
    }

    override fun intercept(testFunction: TestFunction) {
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
