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
package app.cash.treehouse.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch

public interface TreehouseComposition {
  public val diffs: ReceiveChannel<Diff>
  public fun sendEvent(event: Event)
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun TreehouseComposition(
  scope: CoroutineScope,
  onDiff: (Diff) -> Unit = {},
  onEvent: (Event) -> Unit = {},
): TreehouseComposition {
  val server = RealTreehouseComposition(scope, onDiff, onEvent)
  server.launch()
  return server
}

private class RealTreehouseComposition(
  private val scope: CoroutineScope,
  private val onDiff: (Diff) -> Unit,
  private val onEvent: (Event) -> Unit,
) : TreehouseComposition {
  private val diffsChannel = Channel<Diff>(capacity = Channel.UNLIMITED)
  override val diffs = diffsChannel

  private val applier = ProtocolApplier { diff ->
    onDiff(diff)
    diffsChannel.trySend(diff)
  }

  private val recomposer = Recomposer(scope.coroutineContext)
  private val composition = Composition(applier, recomposer)

  private lateinit var snapshotHandle: ObserverHandle
  private var snapshotJob: Job? = null
  private lateinit var recomposeJob: Job

  fun launch() {
    // Set up a trigger to apply changes on the next frame if a global write was observed.
    // TODO where should this live?
    var applyScheduled = false
    snapshotHandle = Snapshot.registerGlobalWriteObserver {
      if (!applyScheduled) {
        applyScheduled = true
        snapshotJob = scope.launch {
          applyScheduled = false
          Snapshot.sendApplyNotifications()
        }
      }
    }

    // These launch undispatched so that they reach their first suspension points before returning
    // control to the caller.
    recomposeJob = scope.launch(start = UNDISPATCHED) {
      recomposer.runRecomposeAndApplyChanges()
    }
  }

  override fun sendEvent(event: Event) {
    onEvent(event)

    val node = applier.nodes[event.id]
    if (node == null) {
      // TODO how to handle race where an incoming event targets this removed node?
      throw IllegalArgumentException("Unknown node ${event.id} for event with tag ${event.tag}")
    }
    node.sendEvent(event)
  }

  override fun setContent(content: @Composable () -> Unit) {
    composition.setContent(content)
  }

  override fun cancel() {
    snapshotHandle.dispose()
    snapshotJob?.cancel()
    recomposeJob.cancel()
    recomposer.cancel()
    diffsChannel.close()
  }
}
