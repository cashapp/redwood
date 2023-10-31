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
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.guest.ProtocolBridge
import app.cash.redwood.protocol.guest.ProtocolRedwoodComposition
import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.UiConfiguration
import app.cash.zipline.ZiplineScope
import app.cash.zipline.ZiplineScoped
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.plus

/**
 * The Kotlin/JS side of a treehouse UI.
 */
public fun TreehouseUi.asZiplineTreehouseUi(
  appLifecycle: StandardAppLifecycle,
): ZiplineTreehouseUi {
  val bridge =
    appLifecycle.protocolBridgeFactory.create(appLifecycle.json, appLifecycle.mismatchHandler)
  return RedwoodZiplineTreehouseUi(appLifecycle, this, bridge)
}

private class RedwoodZiplineTreehouseUi(
  private val appLifecycle: StandardAppLifecycle,
  private val treehouseUi: TreehouseUi,
  private val bridge: ProtocolBridge,
) : ZiplineTreehouseUi, ZiplineScoped, EventSink by bridge {

  /**
   * By overriding [ZiplineScoped.scope], all services passed into [start] are added to this scope,
   * and will all be closed when the scope is closed. This is the only mechanism that can close the
   * host configurations flow.
   */
  override val scope = (treehouseUi as? ZiplineScoped)?.scope ?: ZiplineScope()

  private lateinit var composition: RedwoodComposition

  private lateinit var saveableStateRegistry: SaveableStateRegistry

  @Suppress("OVERRIDE_DEPRECATION")
  override fun start(
    changesSink: ChangesSinkService,
    uiConfigurations: StateFlow<UiConfiguration>,
    stateSnapshot: StateSnapshot?,
  ) {
    start(changesSink, NullOnBackPressedDispatcherService, uiConfigurations, stateSnapshot)
  }

  @Suppress("OVERRIDE_DEPRECATION")
  override fun start(
    changesSink: ChangesSinkService,
    onBackPressedDispatcher: OnBackPressedDispatcherService,
    uiConfigurations: StateFlow<UiConfiguration>,
    stateSnapshot: StateSnapshot?,
  ) {
    val host = object : ZiplineTreehouseUi.Host {
      override val uiConfigurations = uiConfigurations
      override val stateSnapshot = stateSnapshot

      override fun sendChanges(changes: List<Change>) {
        changesSink.sendChanges(changes)
      }

      override fun addOnBackPressedCallback(
        callback: OnBackPressedCallbackService,
      ): CancellableService = onBackPressedDispatcher.addCallback(callback)
    }

    start(host)
  }

  override fun start(host: ZiplineTreehouseUi.Host) {
    this.saveableStateRegistry = SaveableStateRegistry(
      restoredValues = host.stateSnapshot?.content,
      // Note: values will only be restored by SaveableStateRegistry if `canBeSaved` returns true.
      // With current serialization mechanism of stateSnapshot, this field is always true, an update
      // to lambda of this field might be needed when serialization mechanism of stateSnapshot
      // is changed.
      canBeSaved = { true },
    )

    val composition = ProtocolRedwoodComposition(
      scope = appLifecycle.coroutineScope + appLifecycle.frameClock,
      bridge = bridge,
      widgetVersion = appLifecycle.widgetVersion,
      changesSink = host,
      onBackPressedDispatcher = host.asOnBackPressedDispatcher(),
      saveableStateRegistry = saveableStateRegistry,
      uiConfigurations = host.uiConfigurations,
    )
    this.composition = composition

    composition.bind(treehouseUi)
  }

  override fun snapshotState(): StateSnapshot {
    val savedState = saveableStateRegistry.performSave()
    return StateSnapshot(savedState)
  }

  override fun close() {
    composition.cancel()
    treehouseUi.close()
    scope.close()
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
  override var isEnabled: Boolean
    get() = this@asService.isEnabled
    set(value) {
      this@asService.isEnabled = value
    }

  override fun handleOnBackPressed() {
    this@asService.handleOnBackPressed()
  }
}

private object NullOnBackPressedDispatcherService : OnBackPressedDispatcherService {
  override fun addCallback(onBackPressedCallback: OnBackPressedCallbackService) =
    object : CancellableService {
      override fun cancel() = Unit
    }
}
