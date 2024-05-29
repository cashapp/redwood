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
import com.example.redwood.testapp.testing.ButtonValue
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

/**
 * This test focuses on how [TreehouseAppContent] behaves in response to lifecycle events from the
 * code ([CodeSession]), the UI ([TreehouseView]), and the content ([ZiplineTreehouseUi]).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TreehouseAppContentTest {
  private val eventLog = EventLog()
  private val appScope = CoroutineScope(EmptyCoroutineContext)

  private val dispatcher = UnconfinedTestDispatcher()
  private val eventPublisher = FakeEventPublisher()
  private val dispatchers = FakeDispatchers(dispatcher, dispatcher)
  private val onBackPressedDispatcher = FakeOnBackPressedDispatcher(eventLog)
  private val codeHost = FakeCodeHost(
    eventLog = eventLog,
    eventPublisher = eventPublisher,
    dispatchers = dispatchers,
    appScope = appScope,
    frameClockFactory = FakeFrameClock.Factory,
  )
  private val codeEventPublisher = FakeCodeEventPublisher(eventLog)
  private val uiConfiguration = MutableStateFlow(UiConfiguration())

  @BeforeTest
  fun setUp() {
    runBlocking {
      codeHost.start(EventListener.NONE)
      eventLog.takeEvent("codeHostUpdates1.collect()")
    }
  }

  @AfterTest
  fun tearDown() {
    eventLog.assertNoEvents()
    appScope.cancel()
  }

  @Test
  fun bind_session_addWidget_unbind() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    val buttonValue = view1.views.single() as ButtonValue
    assertThat(buttonValue.text).isEqualTo("hello")

    buttonValue.onClick!!.invoke()
    eventLog.takeEvent("codeSessionA.app.uis[0].sendEvent()")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun preload_session_addWidget_bind_unbind() = runTest {
    val content = treehouseAppContent()

    content.preload(onBackPressedDispatcher, uiConfiguration)
    eventLog.assertNoEvents()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    // Guest code can add widgets before a TreehouseView is bound!
    codeSessionA.appService.uis.single().addWidget("hello")
    eventLog.assertNoEvents()

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun session_preload_bind_addWidget_unbind() = runTest {
    val content = treehouseAppContent()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    content.preload(onBackPressedDispatcher, uiConfiguration)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.assertNoEvents()

    codeSessionA.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    val buttonValue = view1.views.single() as ButtonValue
    assertThat(buttonValue.text).isEqualTo("hello")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun session_bind_addWidget_unbind() = runTest {
    val content = treehouseAppContent()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    val buttonValue = view1.views.single() as ButtonValue
    assertThat(buttonValue.text).isEqualTo("hello")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  /** This exercises hot reloading. The view sees new code. */
  @Test
  fun bind_sessionA_sessionB_unbind() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    codeSessionA.appService.uis.single().addWidget("helloA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")

    val codeSessionB = codeHost.startCodeSession("codeSessionB")
    eventLog.takeEventsInAnyOrder(
      "codeSessionA.app.uis[0].close()",
      "codeSessionA.stop()",
      "codeSessionB.start()",
      "codeSessionB.app.uis[0].start()",
    )

    // This still shows UI from codeSessionA. There's no onCodeLoaded() and no reset() until the new
    // code's first widget is added!
    val buttonA = view1.views.single() as ButtonValue
    assertThat(buttonA.text).isEqualTo("helloA")
    codeSessionB.appService.uis.single().addWidget("helloB")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = false)")
    val buttonB = view1.views.single() as ButtonValue
    assertThat(buttonB.text).isEqualTo("helloB")

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
  }

  @Test
  fun preload_unbind_session() = runTest {
    val content = treehouseAppContent()

    content.preload(onBackPressedDispatcher, uiConfiguration)
    eventLog.assertNoEvents()

    content.unbind()
    eventLog.assertNoEvents()

    // Code that arrives after a preloaded UI unbinds doesn't do anything.
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
  }

  @Test
  fun bind_unbind_session() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    content.unbind()
    eventLog.assertNoEvents()

    // Code that arrives after a bound UI unbinds doesn't do anything.
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
  }

  /**
   * Like a TreehouseView being detached and reattached. Each bind yields a completely new
   * ZiplineTreehouseUi because unbind() tears the predecessor down.
   */
  @Test
  fun session_bind_addWidget_unbind_bind_unbind() = runTest {
    val content = treehouseAppContent()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.appService.uis.single().addWidget("helloA")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    val buttonA = view1.views.single() as ButtonValue
    assertThat(buttonA.text).isEqualTo("helloA")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")

    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[1].start()")

    codeSessionA.appService.uis.last().addWidget("helloB")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")
    val buttonB = view1.views.single() as ButtonValue
    assertThat(buttonB.text).isEqualTo("helloB")

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[1].close()")
  }

  @Test
  fun addBackHandler_receives_back_presses_until_canceled() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.clear()

    val backCancelable = codeSessionA.appService.uis.single().addBackHandler(true)
    onBackPressedDispatcher.onBack()
    eventLog.takeEvent("codeSessionA.app.uis[0].onBackPressed()")

    onBackPressedDispatcher.onBack()
    eventLog.takeEvent("codeSessionA.app.uis[0].onBackPressed()")

    backCancelable.cancel()
    eventLog.takeEvent("onBackPressedDispatcher.callbacks[0].cancel()")

    onBackPressedDispatcher.onBack()
    eventLog.assertNoEvents()

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun addBackHandler_receives_no_back_presses_if_disabled() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.clear()

    val backCancelable = codeSessionA.appService.uis.single().addBackHandler(false)
    onBackPressedDispatcher.onBack()
    eventLog.assertNoEvents()

    backCancelable.cancel()

    content.unbind()
    eventLog.takeEvent("onBackPressedDispatcher.callbacks[0].cancel()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun backHandlers_cleared_when_session_changes() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    val codeSessionA = codeHost.startCodeSession("codeSessionA")

    codeSessionA.appService.uis.single().addBackHandler(true)
    assertThat(onBackPressedDispatcher.callbacks).isNotEmpty()

    eventLog.clear()
    codeHost.startCodeSession("codeSessionB")

    // When we close codeSessionA, its back handlers are released with it.
    eventLog.takeEventsInAnyOrder(
      "codeSessionA.app.uis[0].close()",
      "onBackPressedDispatcher.callbacks[0].cancel()",
      "codeSessionA.stop()",
      "codeSessionB.start()",
      "codeSessionB.app.uis[0].start()",
    )
    assertThat(onBackPressedDispatcher.callbacks).isEmpty()

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
  }

  @Test
  fun session_bind_triggerException() = runTest {
    val content = treehouseAppContent()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.handleUncaughtException(Exception("boom!"))
    eventLog.takeEventsInAnyOrder(
      "codeSessionA.app.uis[0].close()",
      "codeListener.onUncaughtException(view1, kotlin.Exception: boom!)",
      "codeSessionA.stop()",
    )

    content.unbind()
  }

  @Test
  fun triggerException_bind_session() = runTest {
    val content = treehouseAppContent()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    codeSessionA.handleUncaughtException(Exception("boom!"))
    eventLog.takeEvent("codeSessionA.stop()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    codeHost.startCodeSession("codeSessionB")
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")

    content.unbind()
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
  }

  @Test
  fun sessionA_bind_triggerException_sessionB() = runTest {
    val content = treehouseAppContent()

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.handleUncaughtException(Exception("boom!"))
    eventLog.takeEventsInAnyOrder(
      "codeSessionA.app.uis[0].close()",
      "codeListener.onUncaughtException(view1, kotlin.Exception: boom!)",
      "codeSessionA.stop()",
    )

    codeHost.startCodeSession("codeSessionB")
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

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    content.preload(onBackPressedDispatcher, uiConfiguration)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.handleUncaughtException(Exception("boom!"))
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    content.unbind()
  }

  @Test
  fun bind_session_addWidget_eventException_unbind() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeSessionA.appService.uis.single().addWidget("hello")
    eventLog.takeEvent("codeListener.onCodeLoaded(view1, initial = true)")

    codeSessionA.appService.uis.single().throwOnNextEvent("boom!")
    val button = view1.views.single() as ButtonValue
    button.onClick!!.invoke()
    eventLog.takeEvent("codeSessionA.app.uis[0].sendEvent()")
    eventLog.takeEvent("codeListener.onUncaughtException(view1, kotlin.Exception: boom!)")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    content.unbind()
  }

  @Test
  fun preload_idempotent() = runTest {
    val content = treehouseAppContent()

    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")

    content.preload(onBackPressedDispatcher, uiConfiguration)
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    content.preload(onBackPressedDispatcher, uiConfiguration)
    eventLog.assertNoEvents()

    content.unbind()
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
  }

  @Test
  fun bind_idempotent() = runTest {
    val content = treehouseAppContent()

    val view1 = treehouseView("view1")
    content.bind(view1)
    eventLog.takeEvent("codeListener.onInitialCodeLoading(view1)")

    content.bind(view1)
    eventLog.assertNoEvents()

    content.unbind()
  }

  private fun treehouseAppContent(): TreehouseAppContent<FakeAppService> {
    return TreehouseAppContent(
      codeHost = codeHost,
      dispatchers = dispatchers,
      codeEventPublisher = codeEventPublisher,
      source = { app -> app.newUi() },
    )
  }

  private fun treehouseView(name: String): FakeTreehouseView {
    return FakeTreehouseView(name, onBackPressedDispatcher, uiConfiguration)
  }
}
