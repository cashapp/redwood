/*
 * Copyright (C) 2023 Square, Inc.
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

import app.cash.redwood.ui.UiConfiguration
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class TreehouseAppContentTest {
  private val eventLog = EventLog()

  private val dispatcher = UnconfinedTestDispatcher()
  private val codeHost = FakeCodeHost<FakeAppService>()
  private val dispatchers = FakeDispatchers(dispatcher, dispatcher)
  private val eventPublisher = FakeEventPublisher()
  private val codeListener = FakeCodeListener(eventLog)
  private val uiConfiguration = UiConfiguration()

  @AfterTest
  fun tearDown() {
    eventLog.assertNoEvents()
  }

  @Test
  fun bind_session_addWidget_unbind() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.session!!.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    assertThat(view1.children.single().value.label).isEqualTo("hello")

    content.unbind()
  }

  @Test
  fun preload_session_addWidget_bind_unbind() = runTest {
    val content = treehouseAppContent()

    content.preload(FakeOnBackPressedDispatcher(), uiConfiguration)
    eventLog.assertNoEvents()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    // Guest code can add widgets before a TreehouseView is bound!
    codeHost.session!!.appService.uis.single().addWidget("hello")
    eventLog.assertNoEvents()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")

    content.unbind()
  }

  @Test
  fun session_preload_bind_addWidget_unbind() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    content.preload(FakeOnBackPressedDispatcher(), uiConfiguration)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.assertNoEvents()

    codeHost.session!!.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    assertThat(view1.children.single().value.label).isEqualTo("hello")

    content.unbind()
  }

  @Test
  fun session_bind_addWidget_unbind() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.session!!.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    assertThat(view1.children.single().value.label).isEqualTo("hello")

    content.unbind()
  }

  /** This exercises hot reloading. The view sees new code. */
  @Test
  fun bind_sessionA_sessionB_unbind() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    codeHost.session!!.appService.uis.single().addWidget("helloA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")

    codeHost.session = FakeCodeSession("codeSessionB", eventLog)
    eventLog.takeEvent("codeSessionA.cancel()")
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")

    // No onCodeLoaded() and no reset() until the new code's first widget is added!
    assertThat(view1.children.single().value.label).isEqualTo("helloA")
    assertThat(eventLog.assertNoEvents())

    codeHost.session!!.appService.uis.single().addWidget("helloB")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = false)")
    assertThat(view1.children.single().value.label).isEqualTo("helloB")

    content.unbind()
  }

  @Test
  fun preload_unbind_session() = runTest {
    val content = treehouseAppContent()

    content.preload(FakeOnBackPressedDispatcher(), uiConfiguration)
    eventLog.assertNoEvents()

    content.unbind()
    eventLog.assertNoEvents()

    // Code that arrives after a preloaded UI unbinds doesn't do anything.
    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")
  }

  @Test
  fun bind_unbind_session() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    content.unbind()
    eventLog.assertNoEvents()

    // Code that arrives after a bound UI unbinds doesn't do anything.
    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")
  }

  /**
   * Like a TreehouseView being detached and reattached. Each bind yields a completely new
   * ZiplineTreehouseUi because unbind() tears the predecessor down.
   */
  @Test
  fun session_bind_addWidget_unbind_bind_unbind() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.session!!.appService.uis.single().addWidget("helloA")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    assertThat(view1.children.single().value.label).isEqualTo("helloA")

    content.unbind()
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[1].start()")

    codeHost.session!!.appService.uis.last().addWidget("helloB")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    assertThat(view1.children.single().value.label).isEqualTo("helloB")

    content.unbind()
  }

  private fun TestScope.treehouseAppContent(): TreehouseAppContent<FakeAppService> {
    return TreehouseAppContent(
      codeHost = codeHost,
      dispatchers = dispatchers,
      appScope = CoroutineScope(coroutineContext),
      eventPublisher = eventPublisher,
      codeListener = codeListener,
      source = { app -> app.newUi() },
    )
  }
}
