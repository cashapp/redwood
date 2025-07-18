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
import kotlinx.dom.appendText

internal class DomPaparazziTest {
  val paparazzi = DomPaparazzi<DomPaparazziTest>()

  class NestedClass

  @Test
  fun nestedClassName() = runTest {
    val paparazzi = DomPaparazzi<NestedClass>()

    val element = document.documentElement!!

    val child = element.appendText("Hello, Nested class")
    paparazzi.snapshot(element, "hello")
    child.remove()
  }

  @Test
  fun printVariables() = runTest {
    // app.cash.redwood.dom.testing
    printKeys("mocha", "suite", "suites", "0", "title")

    // DomPaparazziTest
    printKeys("mocha", "suite", "suites", "0", "suites", "0", "title")

    // helloIAmTheSnapshotTest
    printKeys("mocha", "suite", "suites", "0", "suites", "0", "tests", "0", "title")
  }

  @Test
  fun helloIAmTheSnapshotTest() = runTest {
    val element = document.documentElement!!

    val child = element.appendText("Hello, DOM Paparazzi")
    paparazzi.snapshot(element, "hello")
    child.remove()

    val child2 = element.appendText("TWO")
    paparazzi.snapshot(element, "two")
    child2.remove()
  }

  fun printKeys(vararg names: String) {
    println("${names.joinToString(".")}\n")
    try {
      var value = js("globalThis")
      for (name in names) {
        value = value[name]
      }
      println("type=${jsTypeOf(value)}\n")
      println("value=${value}\n")
      println("keys=${js("Object.keys(value)")}\n")
    } catch (e: Throwable) {
      println(e.stackTraceToString())
    }
    println("\n")
    println("--------------------")
    println("\n")
  }
}
