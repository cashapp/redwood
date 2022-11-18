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
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.currentComposer
import app.cash.redwood.compose.LocalWidgetVersion
import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.compose.WidgetApplier
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.DiffSink
import app.cash.redwood.protocol.Id
import kotlinx.coroutines.CoroutineScope

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
public fun ProtocolRedwoodComposition(
  scope: CoroutineScope,
  factory: DiffProducingWidget.Factory,
  widgetVersion: UInt,
  diffSink: DiffSink,
): RedwoodComposition {
  val protocolState = factory.protocolState
  val rootChildren = DiffProducingWidgetChildren(Id.Root, RootChildrenTag, protocolState)
  val applier = WidgetApplier(factory, rootChildren) {
    protocolState.createDiffOrNull()?.let(diffSink::sendDiff)
  }
  val composition = RedwoodComposition(scope, applier)
  return DiffProducingRedwoodComposition(composition, widgetVersion)
}

private class DiffProducingRedwoodComposition(
  private val composition: RedwoodComposition,
  private val widgetVersion: UInt,
) : RedwoodComposition by composition {
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
}
