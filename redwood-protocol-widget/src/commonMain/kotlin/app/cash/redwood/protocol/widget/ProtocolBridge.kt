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

import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChangesSink
import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenChange.Add
import app.cash.redwood.protocol.ChildrenChange.Move
import app.cash.redwood.protocol.ChildrenChange.Remove
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifierChange
import app.cash.redwood.protocol.LayoutModifierElement
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.widget.Widget
import kotlin.native.ObjCName

/**
 * Bridges the serialized Redwood protocol back to widgets on the display side.
 *
 * This type will consume [Change]s and apply their [ChildrenChange] operations to the widget tree.
 * [PropertyChange]s and [LayoutModifierChange]s are forwarded to their respective widgets.
 * Events from widgets are forwarded to [eventSink].
 */
@ObjCName("ProtocolBridge", exact = true)
public class ProtocolBridge<W : Any>(
  container: Widget.Children<W>,
  private val factory: ProtocolNode.Factory<W>,
  private val eventSink: EventSink,
) : ChangesSink {
  private val nodes = mutableMapOf<Id, ProtocolNode<W>>(
    Id.Root to RootProtocolNode(container),
  )

  override fun sendChanges(changes: List<Change>) {
    for (i in changes.indices) {
      val change = changes[i]
      val id = change.id
      when (change) {
        is Create -> {
          val node = factory.create(change.tag) ?: continue
          val old = nodes.put(change.id, node)
          require(old == null) {
            "Insert attempted to replace existing widget with ID ${change.id.value}"
          }
        }
        is ChildrenChange -> {
          val node = node(id)
          val children = node.children(change.tag) ?: continue
          when (change) {
            is Add -> {
              val child = node(change.childId)
              children.insert(change.index, child.widget)
            }
            is Move -> {
              children.move(change.fromIndex, change.toIndex, change.count)
            }
            is Remove -> {
              children.remove(change.index, change.count)
              @Suppress("ConvertArgumentToSet") // Compose side guarantees set semantics.
              nodes.keys.removeAll(change.removedIds)
            }
          }
        }
        is LayoutModifierChange -> {
          val node = node(id)
          node.updateLayoutModifier(change.elements)
        }
        is PropertyChange -> {
          node(change.id).apply(change, eventSink)
        }
      }
    }
  }

  private fun node(id: Id): ProtocolNode<W> {
    return checkNotNull(nodes[id]) { "Unknown widget ID ${id.value}" }
  }
}

private class RootProtocolNode<W : Any>(
  private val children: Widget.Children<W>,
) : ProtocolNode<W> {
  override fun updateLayoutModifier(elements: List<LayoutModifierElement>) {
    throw AssertionError("unexpected: $elements")
  }

  override fun apply(change: PropertyChange, eventSink: EventSink) {
    throw AssertionError("unexpected: $change")
  }

  override fun children(tag: ChildrenTag) = when (tag) {
    ChildrenTag.Root -> children
    else -> throw AssertionError("unexpected: $tag")
  }
  override val widget: Widget<W> get() = throw AssertionError()

  override fun attachTo(container: Widget.Children<W>) {
    throw AssertionError()
  }
}
