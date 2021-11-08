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
package app.cash.treehouse.widget

import app.cash.treehouse.protocol.ChildrenDiff
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.treehouse.protocol.ChildrenDiff.Companion.RootId
import app.cash.treehouse.protocol.Diff
import app.cash.treehouse.protocol.DiffSink
import app.cash.treehouse.protocol.EventSink
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

public class WidgetDisplay<T : Any>(
  private val root: Widget<T>,
  private val factory: Widget.Factory<T>,
  private val eventSink: EventSink,
  private val serializersModule: SerializersModule = EmptySerializersModule
) : DiffSink {
  init {
    // Check that the root widget has a group of children with the shared root tag. This call
    // will throw if that invariant does not hold.
    root.children(RootChildrenTag)
  }

  private val widgets = mutableMapOf(RootId to root)

  /**
   * Keys in this map are the same keys as in [widgets].
   *
   * TODO(jwilson): change [widgets] to be a `Map<Long, WidgetAndMetadata>` or similar.
   */
  private val widgetIdToKind = mutableMapOf<Long, Int>()

  /** Keys are types, values are serializers for that node type. */
  private val serializersCache = mutableMapOf<Int, Map<Int, KSerializer<*>>>()

  override fun sendDiff(diff: Diff) {
    for (childrenDiff in diff.childrenDiffs) {
      val widget = checkNotNull(widgets[childrenDiff.id]) {
        "Unknown widget ID ${childrenDiff.id}"
      }
      val children = widget.children(childrenDiff.tag)

      when (childrenDiff) {
        is ChildrenDiff.Insert -> {
          val childWidget = factory.create(childrenDiff.kind, childrenDiff.childId)
          widgets[childrenDiff.childId] = childWidget
          children.insert(childrenDiff.index, childWidget.value)
          // TODO(jwilson): this would be nicer if widgets could keep this state?
          widgetIdToKind[childrenDiff.childId] = childrenDiff.kind
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)
          widgets.keys.removeAll(childrenDiff.removedIds)
          widgetIdToKind.keys.removeAll(childrenDiff.removedIds)
        }
        ChildrenDiff.Clear -> {
          children.clear()
          widgets.clear()
          widgets[RootId] = root
          widgetIdToKind.clear()
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val widget = checkNotNull(widgets[propertyDiff.id]) {
        "Unknown widget ID ${propertyDiff.id}"
      }

      val widgetKind = widgetIdToKind[propertyDiff.id]!!
      val serializers = serializersCache.getOrPut(widgetKind) {
        widget.createSerializers(serializersModule)
      }
      widget.apply(serializers, propertyDiff) { event ->
        eventSink.sendEvent(event)
      }
    }
  }
}
