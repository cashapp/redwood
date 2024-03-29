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
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.widget.Widget

/** @suppress For generated code use only. */
@RedwoodCodegenApi
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
