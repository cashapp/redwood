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

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.compose.DiffProducingWidget
import app.cash.redwood.protocol.compose.ProtocolRedwoodComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/**
 * The Kotlin/JS side of a treehouse UI.
 */
public fun TreehouseUi.asZiplineTreehouseUi(
  factory: DiffProducingWidget.Factory,
  widgetVersion: UInt,
): ZiplineTreehouseUi {
  val composition = ProtocolRedwoodComposition(
    scope = scope + frameClock,
    factory = factory,
    widgetVersion = widgetVersion,
  )
  return RedwoodZiplineTreehouseUi(composition, this)
}

private class RedwoodZiplineTreehouseUi(
  private val composition: ProtocolRedwoodComposition,
  private val treehouseUi: TreehouseUi,
) : ZiplineTreehouseUi {
  private var diffSinkToClose: DiffSinkService? = null

  override fun sendEvent(event: Event) {
    composition.sendEvent(event)
  }

  override fun start(
    diffSink: DiffSinkService,
    hostConfigurations: FlowWithInitialValue<HostConfiguration>,
  ) {
    check(diffSinkToClose == null)
    diffSinkToClose = diffSink
    composition.start(diffSink)

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
    diffSinkToClose?.close()
  }
}

private val scope: CoroutineScope = GlobalScope
private val frameClock: BroadcastFrameClock by lazy { newFrameClock(scope) }

// TODO(jwilson): replace this with a native frame clock.
private fun newFrameClock(
  coroutineScope: CoroutineScope,
  ticksPerSecond: Long = 60,
): BroadcastFrameClock {
  val result = BroadcastFrameClock()
  coroutineScope.launch {
    var now = 0L
    val delayNanos = 1_000_000_000L / ticksPerSecond
    while (true) {
      result.sendFrame(now)
      delay(delayNanos / 1_000_000)
      now += delayNanos
    }
  }
  return result
}
