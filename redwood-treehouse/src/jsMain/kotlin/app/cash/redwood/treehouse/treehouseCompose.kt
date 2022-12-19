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

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.compose.ProtocolBridge
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
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
) : ZiplineTreehouseUi, EventSink by bridge {
  private lateinit var diffSinkToClose: DiffSinkService
  private lateinit var composition: RedwoodComposition

  override fun start(
    diffSink: DiffSinkService,
    hostConfigurations: FlowWithInitialValue<HostConfiguration>,
  ) {
    check(!::diffSinkToClose.isInitialized) { "start() can only be called once." }
    diffSinkToClose = diffSink

    val composition = ProtocolRedwoodComposition(
      scope = scope + StandardFrameClock,
      bridge = bridge,
      widgetVersion = widgetVersion,
      diffSink = diffSink,
    )
    this.composition = composition

    val (initialHostConfiguration, hostConfigurationFlow) = hostConfigurations
    composition.setContent {
      val hostConfiguration by hostConfigurationFlow.collectAsState(initialHostConfiguration)
      CompositionLocalProvider(LocalHostConfiguration provides hostConfiguration) {
        treehouseUi.Show()
      }
    }
  }

  override fun close() {
    composition.cancel()
    diffSinkToClose.close()
    treehouseUi.close()
  }
}

@OptIn(DelicateCoroutinesApi::class)
private val scope: CoroutineScope = GlobalScope
