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

import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.protocol.compose.ProtocolMismatchHandler
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
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
  return RedwoodZiplineTreehouseUi(appLifecycle, this)
}

private class RedwoodZiplineTreehouseUi(
  private val appLifecycle: StandardAppLifecycle,
  private val treehouseUi: TreehouseUi,
) : ZiplineTreehouseUi, ZiplineScoped {

  /**
   * By overriding [ZiplineScoped.scope], all services passed into [start] are added to this scope,
   * and will all be closed when the scope is closed. This is the only mechanism that can close the
   * host configurations flow.
   */
  override val scope = (treehouseUi as? ZiplineScoped)?.scope ?: ZiplineScope()

  private lateinit var bridge: ProtocolBridge

  private lateinit var composition: RedwoodComposition

  override fun start(
    diffSink: DiffSinkService,
    hostConfigurations: StateFlow<HostConfiguration>,
  ) {
    bridge = appLifecycle.protocolBridgeFactory.create(
      appLifecycle.json, ProtocolMismatchHandler.Logging,
    )
    val composition = ProtocolRedwoodComposition(
      scope = appLifecycle.coroutineScope + appLifecycle.frameClock,
      bridge = bridge,
      widgetVersion = appLifecycle.widgetVersion,
      diffSink = diffSink,
    )
    this.composition = composition

    composition.bind(treehouseUi, hostConfigurations.value, hostConfigurations)
  }

  override fun close() {
    composition.cancel()
    treehouseUi.close()
    scope.close()
  }

  override fun sendEvent(event: Event) {
    bridge.sendEvent(event)
  }
}
