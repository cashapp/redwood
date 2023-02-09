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
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.zipline.ZiplineScope
import app.cash.zipline.ZiplineScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus

/**
 * The Kotlin/JS side of a treehouse UI.
 */
public fun TreehouseUi.asZiplineTreehouseUi(
  bridge: ProtocolBridge,
  widgetVersion: UInt,
): ZiplineTreehouseUi {
  return RedwoodZiplineTreehouseUi(bridge, widgetVersion, this)
}

private class RedwoodZiplineTreehouseUi(
  private val bridge: ProtocolBridge,
  private val widgetVersion: UInt,
  private val treehouseUi: TreehouseUi,
) : ZiplineTreehouseUi, ZiplineScoped, EventSink by bridge {
  /**
   * By overriding [ZiplineScoped.scope], all services passed into [start] are added to this scope,
   * and will all be closed when the scope is closed. This is the only mechanism that can close the
   * host configurations flow.
   */
  override val scope = (treehouseUi as? ZiplineScoped)?.scope ?: ZiplineScope()

  private lateinit var composition: RedwoodComposition

  override fun start(
    diffSink: DiffSinkService,
    hostConfigurations: FlowWithInitialValue<HostConfiguration>,
  ) {
    val composition = ProtocolRedwoodComposition(
      scope = coroutineScope + StandardFrameClock,
      bridge = bridge,
      widgetVersion = widgetVersion,
      diffSink = diffSink,
    )
    this.composition = composition

    val (initialHostConfiguration, hostConfigurationFlow) = hostConfigurations
    composition.bind(treehouseUi, initialHostConfiguration, hostConfigurationFlow)
  }

  override fun close() {
    composition.cancel()
    treehouseUi.close()
    scope.close()
  }
}

@OptIn(DelicateCoroutinesApi::class)
private val coroutineScope: CoroutineScope = GlobalScope
