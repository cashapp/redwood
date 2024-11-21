/*
 * Copyright (C) 2022 Square, Inc.
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

import androidx.compose.runtime.saveable.SaveableStateRegistry
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.guest.GuestProtocolAdapter
import app.cash.redwood.protocol.guest.ProtocolRedwoodComposition
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.zipline.ZiplineScope
import app.cash.zipline.ZiplineScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

/**
 * The Kotlin/JS side of a treehouse UI.
 */
public fun TreehouseUi.asZiplineTreehouseUi(
  appLifecycle: StandardAppLifecycle,
): ZiplineTreehouseUi {
  val guestAdapter = GuestProtocolAdapter(
    hostVersion = appLifecycle.hostProtocolVersion,
    json = appLifecycle.json,
    widgetSystemFactory = appLifecycle.protocolWidgetSystemFactory,
    mismatchHandler = appLifecycle.mismatchHandler,
  )
  return RedwoodZiplineTreehouseUi(appLifecycle, this, guestAdapter)
}

private class RedwoodZiplineTreehouseUi(
  private val appLifecycle: StandardAppLifecycle,
  private val treehouseUi: TreehouseUi,
  private val guestAdapter: GuestProtocolAdapter,
) : ZiplineTreehouseUi,
  ZiplineScoped,
  EventSink by guestAdapter,
  StandardAppLifecycle.FrameListener {

  override fun onFrame(timeNanos: Long) {
    guestAdapter.emitChanges()
  }

  /**
   * By overriding [ZiplineScoped.scope], all services passed into [start] are added to this scope,
   * and will all be closed when the scope is closed. This is the only mechanism that can close the
   * host configurations flow.
   */
  override val scope = (treehouseUi as? ZiplineScoped)?.scope ?: ZiplineScope()

  private val coroutineScope = CoroutineScope(
    appLifecycle.coroutineScope.coroutineContext +
      Job(appLifecycle.coroutineScope.coroutineContext.job),
  )

  private lateinit var composition: RedwoodComposition

  private lateinit var saveableStateRegistry: SaveableStateRegistry

  override fun start(host: ZiplineTreehouseUi.Host) {
    this.saveableStateRegistry = SaveableStateRegistry(
      restoredValues = host.stateSnapshot?.content,
      // Note: values will only be restored by SaveableStateRegistry if `canBeSaved` returns true.
      // With current serialization mechanism of stateSnapshot, this field is always true, an update
      // to lambda of this field might be needed when serialization mechanism of stateSnapshot
      // is changed.
      canBeSaved = { true },
    )

    guestAdapter.initChangesSink(host)

    appLifecycle.addFrameListener(this)

    val composition = ProtocolRedwoodComposition(
      scope = coroutineScope + appLifecycle.frameClock,
      guestAdapter = guestAdapter,
      widgetVersion = appLifecycle.widgetVersion,
      onBackPressedDispatcher = host.asOnBackPressedDispatcher(),
      saveableStateRegistry = saveableStateRegistry,
      uiConfigurations = host.uiConfigurations,
    )
    this.composition = composition

    composition.bind(treehouseUi)

    // Explicitly emit the initial changes produced by calling 'setContent' (within 'bind').
    // All other changes are initiated and emitted by the [onFrame] callback.
    guestAdapter.emitChanges()
  }

  override fun snapshotState(): StateSnapshot {
    val savedState = saveableStateRegistry.performSave()
    return StateSnapshot(savedState)
  }

  override fun close() {
    appLifecycle.removeFrameListener(this)
    coroutineScope.coroutineContext.job.invokeOnCompletion {
      scope.close()
    }
    coroutineScope.cancel()
    composition.cancel()
    treehouseUi.close()
  }
}

private fun ZiplineTreehouseUi.Host.asOnBackPressedDispatcher() = object : OnBackPressedDispatcher {
  override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
    return this@asOnBackPressedDispatcher.addOnBackPressedCallback(
      onBackPressedCallback.asService(),
    )
  }
}

private fun OnBackPressedCallback.asService() = object : OnBackPressedCallbackService {
  override val isEnabled = MutableStateFlow(this@asService.isEnabled)

  init {
    enabledChangedCallback = {
      isEnabled.value = this@asService.isEnabled
    }
  }

  override fun handleOnBackPressed() {
    this@asService.handleOnBackPressed()
  }

  override fun close() {
    enabledChangedCallback = null
  }
}
