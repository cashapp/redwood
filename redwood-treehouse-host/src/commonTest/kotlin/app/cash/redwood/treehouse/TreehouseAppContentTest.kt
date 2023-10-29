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
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
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
  private val codeHost = FakeCodeHost()
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
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
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
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
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
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
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
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  /** This exercises hot reloading. The view sees new code. */
  @Test
  fun bind_sessionA_sessionB_unbind() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    val codeSessionA = FakeCodeSession("codeSessionA", eventLog)
    codeHost.session = codeSessionA
    codeSessionA.appService.uis.single().addWidget("helloA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")

    val codeSessionB = FakeCodeSession("codeSessionB", eventLog)
    codeHost.session = codeSessionB
    eventLog.takeEvent("codeSessionA.cancel()")
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")

    // This still shows UI from codeSessionA. There's no onCodeLoaded() and no reset() until the new
    // code's first widget is added!
    assertThat(view1.children.single().value.label).isEqualTo("helloA")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")

    codeSessionB.appService.uis.single().addWidget("helloB")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = false)")
    assertThat(view1.children.single().value.label).isEqualTo("helloB")

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
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
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")

    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[1].start()")

    codeHost.session!!.appService.uis.last().addWidget("helloB")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    assertThat(view1.children.single().value.label).isEqualTo("helloB")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[1].close()")
  }

  @Test
  fun addBackHandler_receives_back_presses_until_canceled() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.clear()

    val backCancelable = codeHost.session!!.appService.uis.single().addBackHandler(true)
    view1.onBackPressedDispatcher.onBack()
    eventLog.takeEvent("codeSessionA.app.uis[0].onBackPressed()")

    view1.onBackPressedDispatcher.onBack()
    eventLog.takeEvent("codeSessionA.app.uis[0].onBackPressed()")

    backCancelable.cancel()
    view1.onBackPressedDispatcher.onBack()
    eventLog.assertNoEvents()

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun addBackHandler_receives_no_back_presses_if_disabled() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.clear()

    val backCancelable = codeHost.session!!.appService.uis.single().addBackHandler(false)
    view1.onBackPressedDispatcher.onBack()
    eventLog.assertNoEvents()

    backCancelable.cancel()

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun backHandlers_cleared_when_session_changes() = runTest {
    val content = treehouseAppContent()

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    val codeSessionA = FakeCodeSession("codeSessionA", eventLog)
    codeHost.session = codeSessionA

    codeSessionA.appService.uis.single().addBackHandler(true)
    assertThat(view1.onBackPressedDispatcher.callbacks).isNotEmpty()

    val codeSessionB = FakeCodeSession("codeSessionB", eventLog)
    codeHost.session = codeSessionB

    // When we close codeSessionA, its back handlers are released with it.
    assertThat(view1.onBackPressedDispatcher.callbacks).isEmpty()
    eventLog.clear()

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
  }

  @Test
  fun session_bind_triggerException() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.triggerException(Exception("boom!"))
    eventLog.takeEventsInAnyOrder(
      "codeSessionA.app.uis[0].close()",
      "codeListener.onUncaughtException(view1, kotlin.Exception: boom!)",
      "codeSessionA.cancel()",
    )

    content.unbind()
  }

  @Test
  fun triggerException_bind_session() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    codeHost.triggerException(Exception("boom!"))
    eventLog.takeEvent("codeSessionA.cancel()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    codeHost.session = FakeCodeSession("codeSessionB", eventLog)
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
  }

  @Test
  fun sessionA_bind_triggerException_sessionB() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.triggerException(Exception("boom!"))
    eventLog.takeEventsInAnyOrder(
      "codeSessionA.app.uis[0].close()",
      "codeListener.onUncaughtException(view1, kotlin.Exception: boom!)",
      "codeSessionA.cancel()",
    )

    codeHost.session = FakeCodeSession("codeSessionB", eventLog)
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
  }

  /**
   * Exceptions don't notify codeListeners for preloads because there's no view to show an error on.
   * But they do end the current code session.
   */
  @Test
  fun sessionA_preload_triggerException_bind() = runTest {
    val content = treehouseAppContent()

    codeHost.session = FakeCodeSession("codeSessionA", eventLog)
    eventLog.takeEvent("codeSessionA.start()")

    content.preload(FakeOnBackPressedDispatcher(), uiConfiguration)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.triggerException(Exception("boom!"))
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.cancel()")

    val view1 = FakeTreehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

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
