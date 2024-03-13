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
package app.cash.redwood.protocol.widget

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.widget.Widget
import kotlin.math.max
import kotlin.math.min

/**
 * A node which consumes protocol changes and applies them to a platform-specific representation.
 *
 * @suppress
 */
@RedwoodCodegenApi
public abstract class ProtocolNode<W : Any> {
  public abstract val widget: Widget<W>

  /** The index of [widget] within its parent [container]. */
  internal var index: Int = -1

  internal var container: Widget.Children<W>? = null

  public abstract fun apply(change: PropertyChange, eventSink: EventSink)

  public fun updateModifier(modifier: Modifier) {
    widget.modifier = modifier
    container?.onModifierUpdated(index, widget)
  }

  /**
   * Return one of this node's children groups by its [tag].
   *
   * Invalid [tag] values can either produce an exception or result in `null` being returned.
   * If `null` is returned, the caller should make every effort to ignore these children and
   * continue executing.
   */
  public abstract fun children(tag: ChildrenTag): ProtocolChildren<W>?
}

/**
 * @suppress
 */
@RedwoodCodegenApi
public class ProtocolChildren<W : Any>(
  public val children: Widget.Children<W>,
) {
  private val nodes = mutableListOf<ProtocolNode<W>>()

  internal fun insert(index: Int, node: ProtocolNode<W>) {
    nodes.let { nodes ->
      // Bump the index of any nodes which will be shifted.
      for (i in index until nodes.size) {
        nodes[i].index++
      }

      node.index = index
      nodes.add(index, node)
    }

    children.let { children ->
      node.container = children
      children.insert(index, node.widget)
    }
  }

  internal fun remove(index: Int, count: Int) {
    nodes.let { nodes ->
      nodes.remove(index, count)

      // Drop the index of any nodes shifted after the removal.
      for (i in index until nodes.size) {
        nodes[i].index -= count
      }
    }

    children.remove(index, count)
  }

  internal fun move(from: Int, to: Int, count: Int) {
    nodes.let { nodes ->
      nodes.move(from, to, count)

      // If moving up, lower bound is from. If moving down, lower bound is to.
      val lowerBound = min(from, to)
      // If moving up, upper bound is to, If moving down, upper bound is from + count.
      val upperBound = max(to, from + count)
      for (i in lowerBound until upperBound) {
        nodes[i].index = i
      }
    }

    children.move(from, to, count)
  }
}
