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

import android.view.Choreographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/** Use Android's [Choreographer] as a frame source. */
internal actual fun CoroutineScope.tickFrameClock(
  dispatchers: TreehouseDispatchers,
  clockService: FrameClockService,
) {
  // Use a CONFLATED channel so frames are skipped when the Zipline dispatcher can't keep up.
  val latestFrameTimeNanos = Channel<Long>(capacity = CONFLATED)
  coroutineContext.job.invokeOnCompletion {
    latestFrameTimeNanos.close()
  }

  // Send a FrameCallback for every frame. This is an infinite loop in disguise, that only exits
  // when the latestFrameNanos channel is closed.
  launch(dispatchers.ui) {
    val choreographer = Choreographer.getInstance()
    val callback = object : Choreographer.FrameCallback {
      override fun doFrame(frameTimeNanos: Long) {
        val sendResult = latestFrameTimeNanos.trySend(frameTimeNanos)
        if (!sendResult.isSuccess) return
        choreographer.postFrameCallback(this)
      }
    }

    choreographer.postFrameCallback(callback)
  }

  // Collect frames as they arrive.
  launch(dispatchers.zipline) {
    latestFrameTimeNanos.consumeEach { frameTimeNanos ->
      clockService.sendFrame(frameTimeNanos)
    }
  }
}
