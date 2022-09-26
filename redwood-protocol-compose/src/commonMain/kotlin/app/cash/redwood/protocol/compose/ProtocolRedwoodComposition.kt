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
package app.cash.redwood.protocol.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.redwood.compose.LocalWidgetVersion
import app.cash.redwood.protocol.DiffSink
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

public interface ProtocolRedwoodComposition : EventSink {
  public fun start(diffSink: DiffSink)
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun ProtocolRedwoodComposition(
  scope: CoroutineScope,
  factory: DiffProducingWidget.Factory,
  widgetVersion: UInt,
  onDiff: DiffSink = DiffSink {},
  onEvent: EventSink = EventSink {},
): ProtocolRedwoodComposition {
  return DiffProducingRedwoodComposition(scope, factory, widgetVersion, onDiff, onEvent)
}

private class DiffProducingRedwoodComposition(
  private val scope: CoroutineScope,
  private val factory: DiffProducingWidget.Factory,
  private val widgetVersion: UInt,
  private val onDiff: DiffSink,
  private val onEvent: EventSink,
) : ProtocolRedwoodComposition {
  private val recomposer = Recomposer(scope.coroutineContext)

  private lateinit var applier: ProtocolApplier
  private lateinit var composition: Composition

  private lateinit var snapshotHandle: ObserverHandle
  private var snapshotJob: Job? = null
  private lateinit var recomposeJob: Job

  override fun start(diffSink: DiffSink) {
    check(!this::applier.isInitialized) { "display already initialized" }

    val diffAppender = DiffAppender { diff ->
      onDiff.sendDiff(diff)
      diffSink.sendDiff(diff)
    }

    applier = ProtocolApplier(factory, diffAppender)
    composition = Composition(applier, recomposer)

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
    check(this::applier.isInitialized) { "display not initialized" }

    onEvent.sendEvent(event)

    val node = applier.nodes[event.id]
    if (node == null) {
      // TODO how to handle race where an incoming event targets this removed node?
      throw IllegalArgumentException("Unknown node ${event.id} for event with tag ${event.tag}")
    }
    node.sendEvent(event)
  }

  @OptIn(InternalComposeApi::class) // See internal function comment below.
  override fun setContent(content: @Composable () -> Unit) {
    check(this::applier.isInitialized) { "display not initialized" }

    // TODO using CompositionLocalProvider fails to link in release mode with:
    //  inlinable function call in a function with debug info must have a !dbg location
    //    %16 = call i32 @"kfun:kotlin.Array#<get-size>(){}kotlin.Int"(%struct.ObjHeader* %15)
    //  inlinable function call in a function with debug info must have a !dbg location
    //    call void @"kfun:kotlin.Array#<init>(kotlin.Int){}"(%struct.ObjHeader* %18, i32 %17)
    //  inlinable function call in a function with debug info must have a !dbg location
    //    %20 = call i32 @"kfun:kotlin.Array#<get-size>(){}kotlin.Int"(%struct.ObjHeader* %19)
    //  inlinable function call in a function with debug info must have a !dbg location
    //    %24 = call %struct.ObjHeader* @"kfun:kotlin.collections#copyInto__at__kotlin.Array<out|0:0>(kotlin.Array<0:0>;kotlin.Int;kotlin.Int;kotlin.Int){0\C2\A7<kotlin.Any?>}kotlin.Array<0:0>"(%struct.ObjHeader* %21, %struct.ObjHeader* %22, i32 %23, i32 0, i32 %20, %struct.ObjHeader** %13)
    //  inlinable function call in a function with debug info must have a !dbg location
    //    call void @"kfun:androidx.compose.runtime#CompositionLocalProvider(kotlin.Array<out|androidx.compose.runtime.ProvidedValue<*>>...;kotlin.Function2<androidx.compose.runtime.Composer,kotlin.Int,kotlin.Unit>;androidx.compose.runtime.Composer?;kotlin.Int){}"(%struct.ObjHeader* %27, %struct.ObjHeader* %1, %struct.ObjHeader* %3, i32 %28)
    val providers = arrayOf(LocalWidgetVersion provides widgetVersion)
    composition.setContent {
      currentComposer.startProviders(providers)
      content()
      currentComposer.endProviders()
    }
  }

  override fun cancel() {
    snapshotHandle.dispose()
    snapshotJob?.cancel()
    recomposeJob.cancel()
    recomposer.cancel()
  }
}
