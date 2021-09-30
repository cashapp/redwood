/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.treehouse.zipline

import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import app.cash.treehouse.compose.TreehouseComposition
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Event
import app.cash.zipline.FlowReference
import app.cash.zipline.asFlowReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

/** Adapts a TreehouseComposition to a Zipline-friendly interface. */
public interface ZiplineComposition {
  public fun sendEvent(event: Event)
  public fun diffs(): FlowReference<Diff>
}

public class RealZiplineComposition(
  coroutineScope: CoroutineScope
) : ZiplineComposition {
  private val frameClock = newFrameClock(coroutineScope)
  private val diffsFlow = MutableSharedFlow<Diff>(
    replay = Int.MAX_VALUE,
    extraBufferCapacity = Int.MAX_VALUE
  )
  private val composition = TreehouseComposition(
    scope = coroutineScope + frameClock,
    display = { diff, _ -> check(diffsFlow.tryEmit(diff)) },
    onDiff = { println("TreehouseDiff $it") },
    onEvent = { println("TreehouseEvent $it") },
  )

  public fun setContent(content: @Composable () -> Unit) {
    composition.setContent { content() }
  }

  override fun sendEvent(event: Event) {
    composition.sendEvent(event)
  }

  override fun diffs(): FlowReference<Diff> {
    return diffsFlow.asFlowReference()
  }
}

private fun newFrameClock(
  coroutineScope: CoroutineScope,
  ticksPerSecond: Long = 60
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
