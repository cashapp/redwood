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

import app.cash.redwood.treehouse.leaks.LeakWatcher
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.example.redwood.testapp.testing.TextInputValue
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class LeaksTest {
  @Test
  fun widgetNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()
    val content = tester.content(treehouseApp)
    val view = tester.view()

    content.bind(view)

    content.awaitContent(untilChangeCount = 1)
    val textInputValue = view.views.single() as TextInputValue
    assertThat(textInputValue.text).isEqualTo("what would you like to see?")

    val widgetLeakWatcher = LeakWatcher {
      view.children.widgets.single()
    }

    // While the widget is in the UI, it's expected to be in a reference cycle.
    widgetLeakWatcher.assertObjectInReferenceCycle()

    textInputValue.onChange!!.invoke("Empty")

    tester.sendFrame()
    content.awaitContent(untilChangeCount = 2)
    assertThat(view.views).isEmpty()

    // Once the widget is removed, the cycle must be broken and the widget must be unreachable.
    widgetLeakWatcher.assertNotLeaked()

    treehouseApp.stop()
  }

  @Test
  fun serviceNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()
    treehouseApp.start()
    tester.eventLog.takeEvent("test_app.codeLoadSuccess()", skipOthers = true)

    // Wait for Zipline to be ready.
    // TODO(jwilson): consider deferring events or exposing the TreehouseApp state. As-is the
    //     codeLoadSuccess() event occurs on the Zipline dispatcher but we don't have an instance
    //     here until we bounce that information to the main dispatcher.
    treehouseApp.zipline.first { it != null }

    val serviceLeakWatcher = LeakWatcher {
      tester.hostApi // The first instance of HostApi is held by the current run of the test app.
    }

    // Stop referencing this HostApi from our test harness.
    tester.hostApi = FakeHostApi()

    // Stop the app. Even though we still reference the app, it stops referencing hostApi.
    treehouseApp.stop()
    tester.eventLog.takeEvent("test_app.codeUnloaded()", skipOthers = true)
    serviceLeakWatcher.assertNotLeaked()
  }

  @Test
  fun eventListenerNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    tester.eventListenerFactory = RetainEverythingEventListenerFactory(tester.eventLog)
    val treehouseApp = tester.loadApp()
    val content = tester.content(treehouseApp)
    val view = tester.view()

    content.bind(view)
    content.awaitContent(untilChangeCount = 1)

    val eventListenerLeakWatcher = LeakWatcher {
      (tester.eventListenerFactory as RetainEverythingEventListenerFactory)
        .also {
          assertThat(it.app).isNotNull()
          assertThat(it.manifestUrl).isNotNull()
          assertThat(it.zipline).isNotNull()
          assertThat(it.ziplineManifest).isNotNull()
        }
    }

    // Stop referencing our EventListener from our test harness.
    tester.eventListenerFactory = FakeEventListener.Factory(tester.eventLog)

    // While the listener is in a running app, it's expected to be in a reference cycle.
    eventListenerLeakWatcher.assertObjectInReferenceCycle()

    // It's still in a reference cycle after 'stop', because it can be started again.
    treehouseApp.stop()
    treehouseApp.zipline.first { it == null }
    tester.eventLog.takeEvent("codeUnloaded", skipOthers = true)
    eventListenerLeakWatcher.assertObjectInReferenceCycle()

    // But after close, it's unreachable.
    treehouseApp.close()
    eventListenerLeakWatcher.assertNotLeaked()
  }
}
