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

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Applier
import app.cash.redwood.widget.Widget

/**
 * An [Applier] for a tree of [Widget]s.
 *
 * This applier has special handling for emulating nodes which contain multiple children. Nodes in
 * the tree are required to alternate between [ChildrenWidget] instances and user [Widget] subtypes
 * starting at the root. This invariant is maintained by virtue of the fact that all of the input
 * `@Composables` should be generated Redwood code.
 *
 * For example, a widget tree may look like this:
 * ```
 *                    Children(tag=1)
 *                     /          \
 *                    /            \
 *            ToolbarNode        ListNode
 *             ·     ·                 ·
 *            ·       ·                 ·
 * Children(tag=1)  Children(tag=2)   Children(tag=1)
 *        |              |               /       \
 *        |              |              /         \
 *   ButtonNode     ButtonNode     TextNode     TextNode
 * ```
 * The tree produced by this applier is not a real tree. We do not maintain any relationship from
 * user widgets to the synthetic children widgets as they can never be individually moved/removed.
 * The hierarchy is maintained by Compose's slot table and is represented by dotted lines above.
 */
public class WidgetApplier<W : Any>(
  public val provider: Widget.Provider<W>,
  root: Widget.Children<W>,
  private val onEndChanges: () -> Unit = {},
) : AbstractApplier<Widget<W>>(ChildrenWidget(root)) {
  private var closed = false

  override fun onEndChanges() {
    check(!closed)

    onEndChanges.invoke()
  }

  override fun insertTopDown(index: Int, instance: Widget<W>) {
    check(!closed)

    if (instance is ChildrenWidget) {
      instance.children = instance.accessor!!.invoke(current)
      instance.accessor = null
    } else {
      val current = current as ChildrenWidget
      current.children!!.insert(index, instance)
    }
  }

  override fun insertBottomUp(index: Int, instance: Widget<W>) {
    // Ignored, we insert top-down.
  }

  override fun remove(index: Int, count: Int) {
    check(!closed)

    val current = current as ChildrenWidget
    current.children!!.remove(index, count)
  }

  override fun move(from: Int, to: Int, count: Int) {
    check(!closed)

    val current = current as ChildrenWidget
    current.children!!.move(from, to, count)
  }

  override fun onClear() {
    closed = true
  }
}
