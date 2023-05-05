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
import app.cash.redwood.protocol.LayoutModifierElement
import app.cash.redwood.protocol.LayoutModifiers
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.widget.Widget
import kotlin.native.ObjCName

/**
 * Bridges the serialized Redwood protocol back to widgets on the display side.
 *
 * This type will consume [Diff]s and apply their [ChildrenDiff] operations to the widget tree.
 * [PropertyDiff]s and [LayoutModifiers]s are forwarded to their respective widgets. Events from
 * widgets are forwarded to [eventSink].
 */
@ObjCName("ProtocolBridge", exact = true)
public class ProtocolBridge<W : Any>(
  container: Widget.Children<W>,
  private val factory: ProtocolNode.Factory<W>,
  private val eventSink: EventSink,
) : DiffSink {
  private val nodes = mutableMapOf<Id, ProtocolNode<W>>(
    Id.Root to RootProtocolNode(container),
  )

  override fun sendDiff(diff: Diff) {
    for (childrenDiff in diff.childrenDiffs) {
      val id = childrenDiff.id
      val node = node(id)
      val children = node.children(childrenDiff.tag) ?: continue

      when (childrenDiff) {
        is ChildrenDiff.Insert -> {
          val childWidget = factory.create(childrenDiff.widgetTag) ?: continue
          children.insert(childrenDiff.index, childWidget.widget)
          childWidget.attachTo(children)
          val old = nodes.put(childrenDiff.childId, childWidget)
          require(old == null) {
            "Insert attempted to replace existing widget with ID ${childrenDiff.childId.value}"
          }
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)
        }
      }
    }

    for (layoutModifier in diff.layoutModifiers) {
      val node = node(layoutModifier.id)
      node.updateLayoutModifier(layoutModifier.elements)
    }

    for (propertyDiff in diff.propertyDiffs) {
      node(propertyDiff.id).apply(propertyDiff, eventSink)
    }
  }

  private fun node(id: Id): ProtocolNode<W> {
    return checkNotNull(nodes[id]) { "Unknown widget ID $id" }
  }
}

private class RootProtocolNode<W : Any>(
  private val children: Widget.Children<W>,
) : ProtocolNode<W> {
  override fun updateLayoutModifier(elements: List<LayoutModifierElement>) {
    throw AssertionError("unexpected: $elements")
  }

  override fun apply(diff: PropertyDiff, eventSink: EventSink) {
    throw AssertionError("unexpected: $diff")
  }

  override fun children(tag: ChildrenTag) = when (tag) {
    ChildrenTag.Root -> children
    else -> throw AssertionError("unexpected: $tag")
  }
  override val widget: Widget<W> get() = throw AssertionError()

  override fun attachTo(children: Widget.Children<W>) {
    throw AssertionError()
  }
}
