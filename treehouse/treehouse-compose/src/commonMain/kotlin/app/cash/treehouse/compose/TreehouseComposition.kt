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
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Updater
import androidx.compose.runtime.currentComposer
import app.cash.treehouse.widget.Widget
import kotlinx.coroutines.CoroutineScope
import kotlin.DeprecationLevel.ERROR

public interface TreehouseComposition {
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
@Deprecated("Not implemented yet", level = ERROR)
public fun <T : Any> TreehouseComposition(
  scope: CoroutineScope,
  root: Widget<T>,
  factory: Widget.Factory<T>,
): TreehouseComposition {
  TODO()
}

/**
 * A version of [ComposeNode] which exposes the applier to the [factory] function. Through this
 * we expose the factory type [F] to our factory function so the correct widget can be created.
 *
 * @suppress For generated code usage only.
 */
@Composable
public inline fun <F : Widget.Factory<*>, W : Widget<*>> TreehouseComposeNode(
  crossinline factory: (F) -> W,
  update: @DisallowComposableCalls Updater<W>.() -> Unit,
  content: @Composable () -> Unit,
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
    val applier = currentComposer.applier as TreehouseApplier<F>
    currentComposer.createNode {
      factory(applier.factory)
    }
  } else {
    currentComposer.useNode()
  }

  Updater<W>(currentComposer).update()
  content()

  currentComposer.endNode()
}

/**
 * @suppress For generated code usage only.
 */
public interface TreehouseApplier<F : Widget.Factory<*>> {
  public val factory: F
}
