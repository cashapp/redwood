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
@file:OptIn(ExperimentalJsExport::class)

package app.cash.redwood.dom.testing

import app.cash.burst.Burst
import app.cash.burst.burstValues
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import kotlinx.dom.appendElement
import kotlinx.dom.appendText
import kotlinx.dom.clear

/**
 * This isn't a proper unit test for [SnapshotTester], it's just a sample.
 */
@Burst
internal class SnapshotTesterSampleTest {
  private val snapshotTester = SnapshotTester(
    path = "app.cash.redwood.dom.testing/SnapshotTesterSampleTest",
  )

  @Test
  fun happyPath() = runTest {
    val helloWorld = document.createElement("div").apply {
      appendElement("h1") {
        appendText("hello world")
      }
    }

    snapshotTester.snapshot(helloWorld, "happyPath", Frame.None)
  }

  @Test
  fun mismatchedSnapshot() = runTest {
    val helloWorld = document.createElement("div").apply {
      appendElement("h1") {
        appendText("hello world")
      }
    }
    snapshotTester.snapshot(helloWorld, "mismatchedSnapshot", Frame.None)

    helloWorld.apply {
      clear()
      appendElement("h2") {
        appendText("hello world")
      }
    }
    assertFailsWith<SnapshotMismatchException> {
      snapshotTester.snapshot(helloWorld, "mismatchedSnapshot", Frame.None)
    }
  }

  @Test
  fun exactSizeWithFrame(
    frame: Frame = burstValues(Frame.None, Frame.Iphone14),
  ) = runTest {
    val yellowRect = document.createElement("div").apply {
      setAttribute(
        "style",
        """
        |background-color: #ffff00;
        |width: 200px;
        |height: 100px;
        |position: relative
        """.trimMargin(),
      )
      appendChild(
        document.createElement("div").apply {
          setAttribute(
            "style",
            """
            |background-color: #0000ff;
            |position: absolute;
            |width: 50px;
            |height: 25px;
            |top: 10px;
            |right: 20px;
            """.trimMargin(),
          )
        },
      )
    }

    snapshotTester.snapshot(
      yellowRect,
      "exactSizeWithFrame_${frame.width ?: "wrap"}_x_${frame.height ?: "wrap"}",
      frame,
    )
  }
}
