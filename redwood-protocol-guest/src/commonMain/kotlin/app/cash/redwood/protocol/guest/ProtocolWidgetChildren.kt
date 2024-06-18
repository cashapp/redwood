/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.redwood.protocol.guest

import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.widget.Widget

/** @suppress For generated code use only. */
@RedwoodCodegenApi
public class ProtocolWidgetChildren(
  private val id: Id,
  private val tag: ChildrenTag,
  private val guestAdapter: GuestProtocolAdapter,
) : Widget.Children<Unit> {
  private val _widgets = mutableListOf<ProtocolWidget>()
  override val widgets: List<ProtocolWidget> get() = _widgets

  override fun insert(index: Int, widget: Widget<Unit>) {
    widget as ProtocolWidget
    _widgets.add(index, widget)
    guestAdapter.appendAdd(id, tag, index, widget)
  }

  override fun remove(index: Int, count: Int) {
    if (guestAdapter.synthesizeSubtreeRemoval) {
      val removedIds = ArrayList<Id>(count)
      for (i in index until index + count) {
        val widget = _widgets[i]
        removedIds += widget.id
        guestAdapter.removeWidget(widget.id)

        widget.depthFirstWalk { parent, childrenTag, children ->
          val childIds = children.widgets.map(ProtocolWidget::id)
          for (childId in childIds) {
            guestAdapter.removeWidget(childId)
          }
          guestAdapter.appendRemove(parent.id, childrenTag, 0, childIds.size, childIds)
        }
      }
      guestAdapter.appendRemove(id, tag, index, count, removedIds)
    } else {
      for (i in index until index + count) {
        val widget = _widgets[i]
        guestAdapter.removeWidget(widget.id)
        widget.depthFirstWalk { _, _, children ->
          for (childWidget in children.widgets) {
            guestAdapter.removeWidget(childWidget.id)
          }
        }
      }
      guestAdapter.appendRemove(id, tag, index, count)
    }

    _widgets.remove(index, count)
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    _widgets.move(fromIndex, toIndex, count)
    guestAdapter.appendMove(id, tag, fromIndex, toIndex, count)
  }

  override fun onModifierUpdated(index: Int, widget: Widget<Unit>) {
  }

  public fun depthFirstWalk(
    parent: ProtocolWidget,
    block: (ProtocolWidget, ChildrenTag, ProtocolWidgetChildren) -> Unit,
  ) {
    for (widget in widgets) {
      widget.depthFirstWalk(block)
    }
    block(parent, tag, this)
  }

  override fun detach() {
  }
}
