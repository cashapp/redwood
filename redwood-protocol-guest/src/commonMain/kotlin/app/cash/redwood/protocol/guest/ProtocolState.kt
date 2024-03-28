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

import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChildrenChange.Remove
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.widget.Widget

/** @suppress For generated code use only. */
public class ProtocolState {
  private var nextValue = Id.Root.value + 1
  private val widgets = PlatformMap<Int, ProtocolWidget>()
  private var changes = PlatformList<Change>()

  public fun nextId(): Id {
    val value = nextValue
    nextValue = value + 1
    return Id(value)
  }

  public fun append(change: Change) {
    changes.add(change)
  }

  /**
   * Each time we add a node, confirm it's preceded by either a [Create] or a [Remove].
   *
   * If we're adding a node that's been removed, the node should have been detached instead. Rewrite
   * the change log to match what it should have been.
   */
  public fun requireCreateOrDetach(id: Id, widget: ProtocolWidget) {
    if (id == Id.Root) return

    for (i in 0 until changes.size) {
      when (val change = changes[i]) {
        is Create -> {
          if (change.id == widget.id) return // The added node is newly-created.
        }

        is Remove -> {
          if (change.id != id) continue // Wrong remove: different parent.

          val removedIdIndex = change.removedIds.indexOf(widget.id)
          if (removedIdIndex == -1) continue // Wrong remove: different child.

          // We need to remove the detached node's ID from removedIds. Unfortunately old protocol
          // hosts enforce the removedIds size matches the count, so cheat and put -1 in its place.
          val newRemovedIds = change.removedIds.toMutableList()
          newRemovedIds[removedIdIndex] = Id(-1)
          changes[i] = Remove(
            id = change.id,
            tag = change.tag,
            index = change.index,
            count = change.count,
            removedIds = newRemovedIds,
          )
          return // Success.
        }

        else -> Unit
      }
    }

    throw IllegalStateException("no Create or Remove change for added node ${widget.id}")
  }

  /**
   * If there were any calls to [append] since the last call to this function return them as a
   * list and reset the internal list to be empty. This function returns null if there were
   * no calls to [append] since the last invocation.
   */
  public fun getChangesOrNull(): List<Change>? {
    val changes = changes
    if (changes.size == 0) {
      return null
    }
    this.changes = PlatformList()
    return changes.asList()
  }

  public fun addWidget(widget: ProtocolWidget) {
    val idValue = widget.id.value
    check(idValue !in widgets) {
      "Attempted to add widget with ID $idValue but one already exists"
    }
    widgets[idValue] = widget
  }

  public fun removeWidget(id: Id) {
    widgets.remove(id.value)
  }

  public fun getWidget(id: Id): ProtocolWidget? = widgets[id.value]

  public fun widgetChildren(id: Id, tag: ChildrenTag): Widget.Children<Unit> {
    return ProtocolWidgetChildren(id, tag, this)
  }
}
