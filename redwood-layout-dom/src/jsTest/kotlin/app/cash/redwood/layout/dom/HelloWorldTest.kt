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
package app.cash.redwood.layout.dom

import app.cash.redwood.dom.testing.DomPaparazzi
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class HelloWorldTest() {
  private val domPaparazzi = DomPaparazzi()

  @Test
  fun test() = runTest {
    println("CALLING hello.txt")
//    domPaparazzi.snapshot(view: View, name: String? = null)
    println("DONE")

    js(
      """
      console.log(document.URL);
      console.log(Object.keys(globalThis));
      console.log(Object.keys(globalThis.__karma__));
      console.log(JSON.stringify(globalThis.__karma__.info));
      console.log(JSON.stringify(globalThis.__karma__.result));
      console.log(JSON.stringify(globalThis.__karma__.setupContext));
      console.log(JSON.stringify(globalThis.__karma__.config));
    """,
    )

    error("boom!")
  }
}
