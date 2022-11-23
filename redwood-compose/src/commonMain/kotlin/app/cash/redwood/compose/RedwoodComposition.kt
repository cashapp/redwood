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
package app.cash.redwood.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.Updater
import androidx.compose.runtime.currentComposer
import app.cash.redwood.LayoutModifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.widget.Widget
import kotlin.DeprecationLevel.ERROR
import kotlinx.coroutines.CoroutineScope

public interface RedwoodComposition {
  public fun setContent(content: @Composable () -> Unit)
  public fun cancel()
}

/**
 * @param scope A [CoroutineScope] whose [coroutineContext][kotlin.coroutines.CoroutineContext]
 * must have a [MonotonicFrameClock] key which is being ticked.
 */
@Suppress("UNUSED_PARAMETER")
@Deprecated("Not implemented yet", level = ERROR)
public fun <W : Any> RedwoodComposition(
  scope: CoroutineScope,
  container: Widget.Children<W>,
  factory: Widget.Factory<W>,
): RedwoodComposition {
  TODO()
}

/**
 * A version of [ComposeNode] which exposes the applier to the [factory] function. Through this
 * we expose the factory type [F] to our factory function so the correct widget can be created.
 *
 * @suppress For generated code usage only.
 */
@Composable
@RedwoodCodegenApi
public inline fun <F : Widget.Factory<*>, W : Widget<*>> RedwoodComposeNode(
  crossinline factory: (F) -> W,
  update: @DisallowComposableCalls Updater<W>.() -> Unit,
  content: @Composable RedwoodComposeContent<W>.() -> Unit,
) {
  // NOTE: You MUST keep the implementation of this function (or more specifically, the interaction
  //  with currentComposer) in sync with ComposeNode.
  currentComposer.startNode()

  if (currentComposer.inserting) {
    @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
    val applier = currentComposer.applier as WidgetApplier<F>
    currentComposer.createNode {
      @Suppress("UNCHECKED_CAST") // Safe so long as you use generated composition function.
      factory(applier.factory as F)
    }
  } else {
    currentComposer.useNode()
  }

  Updater<W>(currentComposer).update()
  RedwoodComposeContent.Instance.content()

  currentComposer.endNode()
}

/**
 * @suppress For generated code usage only.
 */
@RedwoodCodegenApi
public class RedwoodComposeContent<out W : Widget<*>> {
  @Composable
  public fun into(
    accessor: (W) -> Widget.Children<*>,
    content: @Composable () -> Unit,
  ) {
    ComposeNode<ChildrenWidget<*>, Applier<*>>(
      factory = {
        @Suppress("UNCHECKED_CAST")
        ChildrenWidget(accessor as (Widget<Any>) -> Widget.Children<Any>)
      },
      update = {},
      content = content,
    )
  }

  public companion object {
    public val Instance: RedwoodComposeContent<Nothing> = RedwoodComposeContent()
  }
}

/**
 * A synthetic widget which allows the applier to differentiate between multiple groups of children.
 *
 * Compose's tree assumes each node only has single list of children. Or, put another way, even if
 * you apply multiple children Compose treats them as a single list of child nodes. In order to
 * differentiate between these children lists we introduce synthetic nodes. Every real node which
 * supports one or more groups of children will have one or more of these synthetic nodes as its
 * direct descendants. The nodes which are produced by each group of children will then become the
 * descendants of those synthetic nodes.
 *
 * @suppress For generated code usage only.
 */
internal class ChildrenWidget<W : Any> private constructor(
  var accessor: ((Widget<W>) -> Widget.Children<W>)?,
  var children: Widget.Children<W>?,
) : Widget<W> {
  constructor(accessor: (Widget<W>) -> Widget.Children<W>) : this(accessor, null)
  constructor(children: Widget.Children<W>) : this(null, children)

  override val value: Nothing get() = throw AssertionError()
  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(_) { throw AssertionError() }
}
