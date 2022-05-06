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
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootChildrenTag
import app.cash.redwood.protocol.ChildrenDiff.Companion.RootId
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.DiffSink
import app.cash.redwood.protocol.EventSink

public class ProtocolDisplay<T : Any>(
  private val root: DiffConsumingWidget<T>,
  private val factory: DiffConsumingWidget.Factory<T>,
  private val eventSink: EventSink,
) : DiffSink {
  init {
    // Check that the root widget has a group of children with the shared root tag.
    requireNotNull(root.children(RootChildrenTag)) {
      "Root widget does not support children with tag $RootChildrenTag"
    }
  }

  private val widgets = mutableMapOf(RootId to root)

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
          children.insert(childrenDiff.index, childWidget)
        }
        is ChildrenDiff.Move -> {
          children.move(childrenDiff.fromIndex, childrenDiff.toIndex, childrenDiff.count)
        }
        is ChildrenDiff.Remove -> {
          children.remove(childrenDiff.index, childrenDiff.count)
          widgets.keys.removeAll(childrenDiff.removedIds)
        }
        ChildrenDiff.Clear -> {
          children.clear()
          widgets.clear()
          widgets[RootId] = root
        }
      }
    }

    for (propertyDiff in diff.propertyDiffs) {
      val widget = checkNotNull(widgets[propertyDiff.id]) {
        "Unknown widget ID ${propertyDiff.id}"
      }
      widget.apply(propertyDiff, eventSink)
    }
  }
}
