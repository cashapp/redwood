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
package app.cash.redwood.protocol.compose

import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.widget.Widget

internal class ProtocolWidgetChildren(
  private val id: Id,
  private val tag: ChildrenTag,
  private val state: ProtocolState,
) : Widget.Children<Nothing> {
  private val ids = mutableListOf<Id>()

  override fun insert(index: Int, widget: Widget<Nothing>) {
    widget as ProtocolWidget
    ids.add(index, widget.id)
    state.addWidget(widget)
    state.append(ChildrenChange.Add(id, tag, widget.id, index))
  }

  override fun remove(index: Int, count: Int) {
    val removingIds = ids.subList(index, index + count)
    val removedIds = removingIds.toTypedArray()
    removingIds.clear()

    for (removedId in removedIds) {
      state.removeWidget(removedId)
    }

    state.append(ChildrenChange.Remove(id, tag, index, count, removedIds))
  }

  override fun move(fromIndex: Int, toIndex: Int, count: Int) {
    ids.move(fromIndex, toIndex, count)
    state.append(ChildrenChange.Move(id, tag, fromIndex, toIndex, count))
  }

  override fun onModifierUpdated() {
  }
}
