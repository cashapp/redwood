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

import app.cash.redwood.protocol.ChildrenDiff
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.DiffSink
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifiers
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.widget.Widget
import kotlinx.serialization.json.JsonArray

/**
 * Bridges the serialized Redwood protocol back to widgets on the display side.
 *
 * This type will consume [Diff]s and apply their [ChildrenDiff] operations to the widget tree.
 * [PropertyDiff]s and [LayoutModifiers]s are forwarded to their respective widgets. Events from
 * widgets are forwarded to [eventSink].
 */
public class ProtocolBridge<W : Any>(
  container: Widget.Children<W>,
  private val factory: DiffConsumingNode.Factory<W>,
  private val eventSink: EventSink,
) : DiffSink {
  private val nodes = mutableMapOf<Id, DiffConsumingNode<W>>(
    Id.Root to DiffConsumingProtocolRoot(container),
  )

  override fun sendDiff(diff: Diff) {
    for (childrenDiff in diff.childrenDiffs) {
      val id = childrenDiff.id
      val node = node(id)
      val children = node.children(childrenDiff.tag) ?: continue

      when (childrenDiff) {
        is ChildrenDiff.Insert -> {
          val childWidget = factory.create(id, children, childrenDiff.widgetTag) ?: continue
          children.insert(childrenDiff.index, childWidget.widget)
          val old = nodes.put(childrenDiff.childId, childWidget)
          require(old == null) {
            "Insert attempted to replace existing widget with ID ${childrenDiff.childId.value}"
          }
          node.childIds.add(childrenDiff.index, childrenDiff.childId)
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
          node.childIds.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)
          for (i in childrenDiff.index until childrenDiff.index + childrenDiff.count) {
            nodes.remove(node.childIds[i])
          }
          node.childIds.remove(childrenDiff.index, childrenDiff.count)
        }
      }
    }

    for (layoutModifier in diff.layoutModifiers) {
      val node = node(layoutModifier.id)
      val childIndex = node(node.parentId).childIds.indexOf(layoutModifier.id)
      node.updateLayoutModifier(layoutModifier.elements)
      node.parentChildren.onLayoutModifierUpdated(childIndex)
    }

    for (propertyDiff in diff.propertyDiffs) {
      node(propertyDiff.id).apply(propertyDiff, eventSink)
    }
  }

  private fun node(id: Id): DiffConsumingNode<W> {
    return checkNotNull(nodes[id]) { "Unknown widget ID $id" }
  }
}

private class DiffConsumingProtocolRoot<W : Any>(
  private val children: Widget.Children<W>,
) : DiffConsumingNode<W>(
  parentId = Id.Root, // This value is a lie, but it's never accessed on this node.
  parentChildren = children, // This value is a lie, but it's never accessed on this node.
) {
  override fun updateLayoutModifier(value: JsonArray) {
    throw AssertionError("unexpected: $value")
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    throw AssertionError("unexpected: $diff")
  }

  override fun children(tag: ChildrenTag) = when (tag) {
    ChildrenTag.Root -> children
    else -> throw AssertionError("unexpected: $tag")
  }

  override val widget: Widget<W> get() = throw AssertionError()
}
