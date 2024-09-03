/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.example.redwood.testapp.testing.ButtonValue
import com.example.redwood.testapp.testing.TextInputValue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TreehouseTesterTest {
  @Test
  fun happyPath() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()
    val content = tester.content(treehouseApp)
    val view = tester.view()

    content.bind(view)

    content.awaitContent(1)
    val textInputValue = view.views.single() as TextInputValue
    assertThat(textInputValue.text).isEqualTo("what would you like to see?")
    textInputValue.onChange!!.invoke("TreehouseTesterTestHappyPathStep2")

    tester.sendFrame()
    content.awaitContent(2)
    val buttonValue = view.views.single() as ButtonValue
    assertThat(buttonValue.text).isEqualTo("This is TreehouseTesterTestHappyPathStep2")

    treehouseApp.stop()
    treehouseApp.close()
  }
}
