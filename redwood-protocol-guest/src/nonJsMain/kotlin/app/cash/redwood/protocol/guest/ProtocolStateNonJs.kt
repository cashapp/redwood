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
import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.RedwoodVersion
import app.cash.redwood.protocol.WidgetTag
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/** @suppress For generated code use only. */
@RedwoodCodegenApi
public actual class ProtocolState actual constructor(
  private val json: Json,
  hostVersion: RedwoodVersion,
) {
  private var nextValue = Id.Root.value + 1
  private val widgets = PlatformMap<Int, ProtocolWidget>()
  private var changes = PlatformList<Change>()

  /**
   * Host versions prior to 0.10.0 contained a bug where they did not recursively remove widgets
   * from the protocol map which leaked any child views of a removed node. We can work around this
   * on the guest side by synthesizing removes for every node in the subtree.
   */
  public actual val synthesizeSubtreeRemoval: Boolean = hostVersion < RedwoodVersion("0.10.0-SNAPSHOT")

  public actual fun nextId(): Id {
    val value = nextValue
    nextValue = value + 1
    return Id(value)
  }

  public actual fun appendCreate(
    id: Id,
    tag: WidgetTag,
  ) {
    val id = id
    val tag = tag
    changes.add(Create(id, tag))
  }

  public actual fun <T> appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  ) {
    changes.add(PropertyChange(id, tag, json.encodeToJsonElement(serializer, value)))
  }

  public actual fun appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    value: Boolean,
  ) {
    changes.add(PropertyChange(id, tag, JsonPrimitive(value)))
  }

  public actual fun appendModifierChange(
    id: Id,
    elements: List<ModifierElement>,
  ) {
    changes.add(ModifierChange(id, elements))
  }

  public actual fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    childId: Id,
    index: Int,
  )  {
    changes.add(ChildrenChange.Add(id, tag, childId, index))
  }

  public actual fun appendMove(
    id: Id,
    tag: ChildrenTag,
    fromIndex: Int,
    toIndex: Int,
    count: Int,
  ) {
    changes.add(ChildrenChange.Move(id, tag, fromIndex, toIndex, count))
  }

  public actual fun appendRemove(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    count: Int,
    removedIds: List<Id>,
  ) {
    changes.add(ChildrenChange.Remove(id, tag, index, count, removedIds))
  }

  /**
   * If there were any calls to [append] since the last call to this function return them as a
   * list and reset the internal list to be empty. This function returns null if there were
   * no calls to [append] since the last invocation.
   */
  public actual fun getChangesOrNull(): List<Change>? {
    val changes = changes
    if (changes.size == 0) {
      return null
    }
    this.changes = PlatformList()
    return changes.asList()
  }

  public actual fun addWidget(widget: ProtocolWidget) {
    val idValue = widget.id.value
    check(idValue !in widgets) {
      "Attempted to add widget with ID $idValue but one already exists"
    }
    widgets[idValue] = widget
  }

  public actual fun removeWidget(id: Id) {
    widgets.remove(id.value)
  }

  public actual fun getWidget(id: Id): ProtocolWidget? = widgets[id.value]
}
