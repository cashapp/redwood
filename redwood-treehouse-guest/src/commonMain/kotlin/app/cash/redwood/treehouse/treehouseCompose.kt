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
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.protocol.compose.ProtocolMismatchHandler
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import app.cash.zipline.ZiplineScope
import app.cash.zipline.ZiplineScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.plus
import kotlinx.serialization.json.Json

/**
 * The Kotlin/JS side of a treehouse UI.
 */
public fun TreehouseUi.asZiplineTreehouseUi(
  bridgeFactory: ProtocolBridge.Factory,
  json: Json,
  widgetVersion: UInt,
): ZiplineTreehouseUi {
  return RedwoodZiplineTreehouseUi(bridgeFactory, json, widgetVersion, this)
}

private class RedwoodZiplineTreehouseUi(
  private val bridgeFactory: ProtocolBridge.Factory,
  private val json: Json,
  private val widgetVersion: UInt,
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
    treehouseHost: TreehouseHost,
  ) {
    val mismatchHandler = object : ProtocolMismatchHandler {
      override fun onUnknownEvent(
        widgetTag: WidgetTag,
        tag: EventTag,
      ) {
        treehouseHost.onUnknownEvent(widgetTag, tag)
      }

      override fun onUnknownEventNode(
        id: Id,
        tag: EventTag,
      ) {
        treehouseHost.onUnknownEventNode(id, tag)
      }
    }
    bridge = bridgeFactory.create(json, mismatchHandler)
    val composition = ProtocolRedwoodComposition(
      scope = coroutineScope + StandardFrameClock,
      bridge = bridge,
      widgetVersion = widgetVersion,
      diffSink = diffSink,
    )
    this.composition = composition

    composition.bind(treehouseUi, hostConfigurations.value, hostConfigurations)
  }

  override fun sendEvent(event: Event) {
    bridge.sendEvent(event)
  }

  override fun close() {
    composition.cancel()
    treehouseUi.close()
    scope.close()
  }
}

@OptIn(DelicateCoroutinesApi::class)
private val coroutineScope: CoroutineScope = GlobalScope
