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

import app.cash.redwood.testing.WidgetValue
import app.cash.redwood.treehouse.leaks.LeakWatcher
import app.cash.zipline.Zipline
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.example.redwood.testapp.testing.TextInputValue
import com.example.redwood.testapp.treehouse.TestAppPresenter
import kotlin.test.Test
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    content.awaitContent()

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

  @Test
  fun eventListenerFactoryNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    tester.eventListenerFactory = RetainEverythingEventListenerFactory(tester.eventLog)
    val treehouseApp = tester.loadApp()

    val eventListenerFactoryLeakWatcher = LeakWatcher {
      tester.eventListenerFactory
    }

    // Stop referencing our EventListener.Factory from our test harness.
    tester.eventListenerFactory = FakeEventListener.Factory(tester.eventLog)

    // After close, it's unreachable.
    treehouseApp.close()
    eventListenerFactoryLeakWatcher.assertNotLeaked()
    tester.eventLog.takeEvent("EventListener.Factory.close()", skipOthers = true)
  }

  @Test
  fun contentSourceNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()
    val view = tester.view()

    // Use an inline run() to try to prevent contentSource from being retained on the stack.
    val (content, contentSourceLeakWatcher) = run {
      val contentSource = RetainEverythingTreehouseContentSource()

      val content = treehouseApp.createContent(
        source = contentSource,
      )

      return@run content to LeakWatcher { contentSource }
    }

    // After we bind the content, it'll be in a retain cycle.
    content.bind(view)
    content.awaitCodeLoaded(loadCount = 1)
    contentSourceLeakWatcher.assertObjectInReferenceCycle()

    // Unbind it, and it's no longer retained.
    content.unbind()
    content.awaitCodeDetached()
    treehouseApp.dispatchers.awaitLaunchedTasks()
    contentSourceLeakWatcher.assertNotLeaked()

    treehouseApp.stop()
  }

  @Test
  fun redwoodViewNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()
    var view: TreehouseView<WidgetValue>? = tester.view()

    val content = treehouseApp.createContent(
      source = { app -> app.launchForTester() },
    )
    val viewLeakWatcher = LeakWatcher {
      view
    }

    // One content is bound, the view is in a reference cycle.
    content.bind(view!!)
    content.awaitCodeLoaded(loadCount = 1)
    viewLeakWatcher.assertObjectInReferenceCycle()

    // Stop referencing the view from our test harness.
    view = null

    // When content is unbound, the view is no longer reachable.
    content.unbind()
    treehouseApp.dispatchers.awaitLaunchedTasks()
    viewLeakWatcher.assertNotLeaked()

    treehouseApp.stop()
  }

  @Test
  fun appSpecNotLeaked() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()

    val specLeakWatcher = LeakWatcher {
      tester.spec
    }

    // Stop referencing our TreehouseApp.Spec from our test harness.
    tester.spec = object : TreehouseApp.Spec<TestAppPresenter>() {
      override val name: String
        get() = error("unexpected call")
      override val manifestUrl: Flow<String>
        get() = error("unexpected call")

      override suspend fun bindServices(
        treehouseApp: TreehouseApp<TestAppPresenter>,
        zipline: Zipline,
      ) = error("unexpected call")

      override fun create(zipline: Zipline) = error("unexpected call")
    }

    treehouseApp.close()

    // Once the app is closed, the spec must be unreachable.
    specLeakWatcher.assertNotLeaked()

    // The app's name is always accessible.
    assertThat(treehouseApp.name).isEqualTo("test_app")
  }

  @Test
  fun treehouseDispatchersClosedByApp() = runTest {
    val treehouseTester = TreehouseTester(this)
    val app = treehouseTester.loadApp()
    assertThat(treehouseTester.openTreehouseDispatchersCount).isEqualTo(1)
    app.close()
    assertThat(treehouseTester.openTreehouseDispatchersCount).isEqualTo(0)
  }

  /**
   * Confirm we don't hold an object beyond its required lifecycle. Confirming no cycles exist is
   * more strict than we require for this object: we only need the content to be unreachable.
   */
  @Test
  fun contentNotHeldAfterUnbind() = runTest {
    val tester = TreehouseTester(this)
    val treehouseApp = tester.loadApp()
    val view = tester.view()

    var content: Content? = tester.content(treehouseApp)
    val contentLeakWatcher = LeakWatcher {
      content
    }

    // After we bind the content, it'll be in a retain cycle.
    content!!.bind(view)
    content.awaitCodeLoaded(loadCount = 1)
    contentLeakWatcher.assertObjectInReferenceCycle()

    // Unbind it and stop referencing it in our test harness.
    content.unbind()
    content.awaitCodeDetached()
    content = null

    // The content is no longer retained.
    treehouseApp.dispatchers.awaitLaunchedTasks()
    contentLeakWatcher.assertNotLeaked()

    treehouseApp.stop()
  }

  /**
   * This is unfortunate. Some cleanup functions launch jobs on another dispatcher and we don't have
   * a natural way to wait for those jobs to complete. So we launch empty jobs on each dispatcher,
   * and trust that they won't start until existing queued jobs finish.
   */
  private suspend fun TreehouseDispatchers.awaitLaunchedTasks() {
    coroutineScope {
      launch(zipline) {}.join()
      launch(ui) {}.join()
    }
  }
}
