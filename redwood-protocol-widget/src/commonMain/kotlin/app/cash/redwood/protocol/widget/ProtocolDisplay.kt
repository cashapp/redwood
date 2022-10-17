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
  container: Widget.Children<T>,
  private val factory: DiffConsumingWidget.Factory<T>,
  private val eventSink: EventSink,
) : DiffSink {
  private val root: DiffConsumingWidget<T> = ProtocolDisplayRoot(container)
  private val widgets = mutableMapOf(Id.Root to root)

  override fun sendDiff(diff: Diff) {
    for (childrenDiff in diff.childrenDiffs) {
      val widget = checkNotNull(widgets[childrenDiff.id]) {
        "Unknown widget ID ${childrenDiff.id}"
      }
      val children = widget.children(childrenDiff.tag) ?: continue

      when (childrenDiff) {
        is ChildrenDiff.Insert -> {
          val childWidget = factory.create(childrenDiff.kind) ?: continue
          widgets[childrenDiff.childId] = childWidget
          children.insert(childrenDiff.index, childWidget.value)
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)

          @Suppress("ConvertArgumentToSet") // Bad advice as the collection is iterated.
          widgets.keys.removeAll(childrenDiff.removedIds)
        }
        ChildrenDiff.Clear -> {
          children.clear()
          widgets.clear()
          widgets[Id.Root] = root
        }
      }
    }

    for (layoutModifier in diff.layoutModifiers) {
      val widget = checkNotNull(widgets[layoutModifier.id]) {
        "Unknown widget ID ${layoutModifier.id}"
      }
      widget.updateLayoutModifier(layoutModifier.elements)
    }

    for (propertyDiff in diff.propertyDiffs) {
      val widget = checkNotNull(widgets[propertyDiff.id]) {
        "Unknown widget ID ${propertyDiff.id}"
      }
      widget.apply(propertyDiff, eventSink)
    }
  }
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
