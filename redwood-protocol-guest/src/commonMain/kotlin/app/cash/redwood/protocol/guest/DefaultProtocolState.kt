/*
 * Copyright (C) 2024 Square, Inc.
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
import app.cash.redwood.protocol.ChangesSink
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
public class DefaultProtocolState(
  public override val json: Json = Json.Default,
  hostVersion: RedwoodVersion,
) : ProtocolState {
  private var nextValue = Id.Root.value + 1
  private val widgets = mutableMapOf<Int, ProtocolWidget>()
  private var changes = mutableListOf<Change>()
  private lateinit var changesSink: ChangesSink

  public override val synthesizeSubtreeRemoval: Boolean = hostVersion < RedwoodVersion("0.10.0-SNAPSHOT")

  override fun initChangesSink(changesSink: ChangesSink) {
    this.changesSink = changesSink
  }

  public override fun nextId(): Id {
    val value = nextValue
    nextValue = value + 1
    return Id(value)
  }

  public override fun appendCreate(
    id: Id,
    tag: WidgetTag,
  ) {
    val id = id
    val tag = tag
    changes.add(Create(id, tag))
  }

  public override fun <T> appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  ) {
    changes.add(PropertyChange(id, tag, json.encodeToJsonElement(serializer, value)))
  }

  public override fun appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    value: Boolean,
  ) {
    changes.add(PropertyChange(id, tag, JsonPrimitive(value)))
  }

  public override fun appendModifierChange(
    id: Id,
    elements: List<ModifierElement>,
  ) {
    changes.add(ModifierChange(id, elements))
  }

  public override fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    childId: Id,
    index: Int,
  ) {
    changes.add(ChildrenChange.Add(id, tag, childId, index))
  }

  public override fun appendMove(
    id: Id,
    tag: ChildrenTag,
    fromIndex: Int,
    toIndex: Int,
    count: Int,
  ) {
    changes.add(ChildrenChange.Move(id, tag, fromIndex, toIndex, count))
  }

  public override fun appendRemove(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    count: Int,
    removedIds: List<Id>,
  ) {
    changes.add(ChildrenChange.Remove(id, tag, index, count, removedIds))
  }

  /** Returns the changes accumulated since the last call to this function. */
  public fun takeChanges(): List<Change> {
    val result = changes.toList()
    this.changes.clear()
    return result
  }

  override fun emitChanges() {
    changesSink.sendChanges(takeChanges())
  }

  public override fun addWidget(widget: ProtocolWidget) {
    val idValue = widget.id.value
    check(idValue !in widgets) {
      "Attempted to add widget with ID $idValue but one already exists"
    }
    widgets[idValue] = widget
  }

  public override fun removeWidget(id: Id) {
    widgets.remove(id.value)
  }

  public override fun getWidget(id: Id): ProtocolWidget? = widgets[id.value]
}
