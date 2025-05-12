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

import app.cash.redwood.leaks.LeakDetector
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

/** This test focuses on how [CodeHost] and its state machine. */
@OptIn(ExperimentalCoroutinesApi::class)
class CodeHostTest {
  private val eventLog = EventLog()
  private val appScope = CoroutineScope(EmptyCoroutineContext)

  private val dispatcher = UnconfinedTestDispatcher()
  private val eventPublisher = FakeEventPublisher()
  private val dispatchers = FakeDispatchers(dispatcher, dispatcher)
  private val codeHost = FakeCodeHost(
    eventLog = eventLog,
    eventPublisher = eventPublisher,
    dispatchers = dispatchers,
    appScope = appScope,
  )
  private val onBackPressedDispatcher = FakeOnBackPressedDispatcher(eventLog)

  @AfterTest
  fun tearDown() {
    eventLog.assertNoEvents()
    appScope.cancel()
  }

  /** Confirm that we can bind() before CodeHost.start(). */
  @Test
  fun bind_start_session_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    content.unbind()
  }

  /** Calling CodeHost.restart() from idle starts it up. */
  @Test
  fun bind_restart_session_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.restart()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    content.unbind()
  }

  /** CodeHost doesn't have to stay resident forever. */
  @Test
  fun bind_start_session_stop_start_session_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates2.collect()")
    codeHost.startCodeSession("codeSessionB")
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")
    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates2.close()")
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
    eventLog.takeEvent("codeSessionB.stop()")

    content.unbind()
  }

  /** CodeHost can restart() after a failure. */
  @Test
  fun bind_start_session_crash_restart_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    codeSessionA.handleUncaughtException(Exception("boom!"))
    content.awaitCodeDetached("boom!")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    codeHost.restart()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeHostUpdates2.collect()")
    codeHost.startCodeSession("codeSessionB")
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")
    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates2.close()")
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
    eventLog.takeEvent("codeSessionB.stop()")

    content.unbind()
  }

  /** New code will also trigger a restart after a failure. */
  @Test
  fun bind_start_session_crash_session_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    codeSessionA.handleUncaughtException(Exception("boom!"))
    content.awaitCodeDetached("boom!")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    codeHost.startCodeSession("codeSessionB")
    eventLog.takeEvent("codeSessionB.start()")
    eventLog.takeEvent("codeSessionB.app.uis[0].start()")
    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionB.app.uis[0].close()")
    eventLog.takeEvent("codeSessionB.stop()")

    content.unbind()
  }

  /** We can stop after a failure. */
  @Test
  fun bind_start_session_crash_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    val codeSessionA = codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    codeSessionA.handleUncaughtException(Exception("boom!"))
    content.awaitCodeDetached("boom!")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    codeHost.stop()
    eventLog.takeEvent("codeHostUpdates1.close()")

    content.unbind()
  }

  /** Calling start() while it's starting is a no-op. */
  @Test
  fun bind_start_start_session() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    eventLog.assertNoEvents()

    codeHost.start()
    eventLog.assertNoEvents()

    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")
    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    content.unbind()
  }

  /** Calling start() while it's running is a no-op. */
  @Test
  fun bind_start_session_start_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.start()
    eventLog.assertNoEvents()

    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    content.unbind()
  }

  /** Calling stop() while it's idle is a no-op. */
  @Test
  fun bind_start_session_stop_stop() = runTest {
    val content = treehouseAppContent()
    val view1 = treehouseView("view1")
    content.bind(view1)
    content.awaitInitialCodeLoading()

    codeHost.start()
    eventLog.takeEvent("codeHostUpdates1.collect()")
    codeHost.startCodeSession("codeSessionA")
    eventLog.takeEvent("codeSessionA.start()")
    eventLog.takeEvent("codeSessionA.app.uis[0].start()")

    codeHost.stop()
    content.awaitCodeDetached()
    eventLog.takeEvent("codeHostUpdates1.close()")
    eventLog.takeEvent("codeSessionA.app.uis[0].close()")
    eventLog.takeEvent("codeSessionA.stop()")

    codeHost.stop()

    content.unbind()
  }

  private fun treehouseAppContent(): TreehouseAppContent<FakeAppService> {
    return TreehouseAppContent(
      codeHost = codeHost,
      dispatchers = dispatchers,
      source = { app -> app.newUi() },
      leakDetector = LeakDetector.none(),
    )
  }

  private fun treehouseView(name: String): FakeTreehouseView {
    return FakeTreehouseView(
      name = name,
      onBackPressedDispatcher = onBackPressedDispatcher,
    )
  }
}
