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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.DiffSink
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.widget.Widget
import kotlinx.serialization.json.JsonArray

public class ProtocolDisplay<T : Any>(
  private val container: Widget.Children<T>,
  private val factory: DiffConsumingWidget.Factory<T>,
  private val eventSink: EventSink,
) : DiffSink {
  private val root: DiffConsumingWidget<T> = ProtocolDisplayRoot(container)
  private val nodes = mutableMapOf(Id.Root to Node(root, container))

  override fun sendDiff(diff: Diff) {
    for (childrenDiff in diff.childrenDiffs) {
      val node = node(childrenDiff.id)
      val children = node.widget.children(childrenDiff.tag) ?: continue

      when (childrenDiff) {
        is ChildrenDiff.Insert -> {
          val childWidget = factory.create(childrenDiff.kind) ?: continue
          children.insert(childrenDiff.index, childWidget)
          nodes[childrenDiff.childId] = Node(childWidget, children)
          node.childIds.add(childrenDiff.index, childrenDiff.childId)
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
          node.childIds.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)
          for (i in childrenDiff.index until childrenDiff.index + childrenDiff.count) {
            nodes.keys.remove(node.childIds[i])
          }
          node.childIds.remove(childrenDiff.index, childrenDiff.count)
        }
        ChildrenDiff.Clear -> {
          children.clear()
          node.childIds.clear()
          nodes.clear()
          nodes[Id.Root] = Node(root, container)
        }
      }
    }

    for (layoutModifier in diff.layoutModifiers) {
      val node = node(layoutModifier.id)
      node.widget.updateLayoutModifier(layoutModifier.elements)
      node.parent.updateLayoutModifier(node.widget)
    }

    for (propertyDiff in diff.propertyDiffs) {
      node(propertyDiff.id).widget.apply(propertyDiff, eventSink)
    }
  }

  private fun node(id: Id): Node<T> {
    return checkNotNull(nodes[id]) { "Unknown widget ID $id" }
  }
}

private class Node<T : Any>(
  val widget: DiffConsumingWidget<T>,
  val parent: Widget.Children<T>,
) {
  private var _childIds: MutableList<Id>? = null
  val childIds: MutableList<Id>
    get() = _childIds ?: mutableListOf<Id>().also { _childIds = it }
}

private class ProtocolDisplayRoot<T : Any>(
  private val children: Widget.Children<T>,
) : DiffConsumingWidget<T> {
  override var layoutModifiers: LayoutModifier
    get() = throw AssertionError()
    set(value) = throw AssertionError("unexpected: $value")

  override fun updateLayoutModifier(value: JsonArray) {
    throw AssertionError("unexpected: $value")
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    throw AssertionError("unexpected: $diff")
  }

  override fun children(tag: UInt) = when (tag) {
    RootChildrenTag -> children
    else -> throw AssertionError("unexpected: $tag")
  }

  override val value: T get() = throw AssertionError()
}
