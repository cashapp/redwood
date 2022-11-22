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
import androidx.compose.runtime.snapshots.Snapshot
import app.cash.redwood.compose.LocalWidgetVersion
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.DiffSink
import app.cash.redwood.protocol.Id
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun ProtocolRedwoodComposition(
  scope: CoroutineScope,
  factory: DiffProducingWidget.Factory,
  diffSink: DiffSink,
  widgetVersion: UInt,
): RedwoodComposition {
  val bridge = factory.bridge
  val root = DiffProducingWidgetChildren(Id.Root, ChildrenDiff.RootChildrenTag, bridge)
  val applier = ProtocolApplier(factory, root) {
    bridge.createDiffOrNull()?.let(diffSink::sendDiff)
  }
  return DiffProducingRedwoodComposition(scope, applier, widgetVersion)
}

private class DiffProducingRedwoodComposition(
  private val scope: CoroutineScope,
  applier: ProtocolApplier,
  private val widgetVersion: UInt,
) : RedwoodComposition {
  private val recomposer = Recomposer(scope.coroutineContext)
  private val composition = Composition(applier, recomposer)

  private var snapshotJob: Job? = null
  private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
    // Set up a trigger to apply changes on the next frame if a global write was observed.
    if (snapshotJob == null) {
      snapshotJob = scope.launch {
        snapshotJob = null
        Snapshot.sendApplyNotifications()
      }
    }
  }

  // These launch undispatched so that they reach their first suspension points before returning
  // control to the caller.
  private val recomposeJob = scope.launch(start = UNDISPATCHED) {
    recomposer.runRecomposeAndApplyChanges()
  }

  @OptIn(InternalComposeApi::class) // See internal function comment below.
  override fun setContent(content: @Composable () -> Unit) {
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
