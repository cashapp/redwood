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

import kotlin.test.Test
import kotlinx.browser.document
import kotlinx.coroutines.test.runTest
import kotlinx.dom.appendElement
import kotlinx.dom.appendText

/**
 * This isn't a proper unit test for [DomSnapshotter], it's just a sample.
 */
internal class DomSnapshotterSampleTest {
  val snapshotter = DomSnapshotter("DomPaparazziTest")

  @Test
  fun happyPath() = runTest {
    val element = document.documentElement!!

    element.appendElement("h1") {
      appendText("hello world")
    }
    snapshotter.snapshot(element, "helloIAmTheSnapshotTest")
  }
}
