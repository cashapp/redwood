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

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChangesSink
import app.cash.redwood.protocol.ChildrenChange
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.RedwoodVersion
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.WidgetSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/** @suppress For generated code use only. */
@OptIn(RedwoodCodegenApi::class)
public class DefaultGuestProtocolAdapter(
  public override val json: Json = Json.Default,
  hostVersion: RedwoodVersion,
  private val widgetSystemFactory: ProtocolWidgetSystemFactory,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : GuestProtocolAdapter(hostVersion) {
  private var nextValue = Id.Root.value + 1
  private val widgets = mutableMapOf<Int, ProtocolWidget>()
  private val removed = mutableSetOf<Int>()
  private val changes = mutableListOf<Change>()
  private lateinit var changesSink: ChangesSink

  public override val widgetSystem: WidgetSystem<Unit> =
    widgetSystemFactory.create(this, mismatchHandler)

  public override val root: Widget.Children<Unit> =
    ProtocolWidgetChildren(Id.Root, ChildrenTag.Root, this)

  override fun sendEvent(event: Event) {
    val node = widgets[event.id.value]
    if (node != null) {
      node.sendEvent(event)
    } else {
      mismatchHandler.onUnknownEventNode(event.id, event.tag)
    }
  }

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
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  ) {
    changes.add(PropertyChange(id, widgetTag, propertyTag, json.encodeToJsonElement(serializer, value)))
  }

  public override fun appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    value: Boolean,
  ) {
    changes.add(PropertyChange(id, widgetTag, propertyTag, JsonPrimitive(value)))
  }

  @OptIn(ExperimentalSerializationApi::class)
  override fun appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    value: UInt,
  ) {
    changes.add(PropertyChange(id, widgetTag, propertyTag, JsonPrimitive(value)))
  }

  override fun appendModifierChange(id: Id, value: Modifier) {
    val elements = mutableListOf<ModifierElement>()

    value.forEach { element ->
      elements += modifierElement(element)
    }

    changes.add(ModifierChange(id, elements))
  }

  private fun <T : Modifier.Element> modifierElement(element: T): ModifierElement {
    val (tag, serializer) = widgetSystemFactory.modifierTagAndSerializationStrategy(element)
    if (serializer == null) return ModifierElement(tag)
    return ModifierElement(tag, json.encodeToJsonElement(serializer, element))
  }

  public override fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    child: ProtocolWidget,
  ) {
    val replaced = widgets.put(child.id.value, child)
    check(replaced == null) {
      "Attempted to add widget with ID ${child.id} but one already exists"
    }
    changes.add(ChildrenChange.Add(id, tag, child.id, index))
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
    childId: Id,
  ) {
    removed += childId.value
    changes.add(ChildrenChange.Remove(id, tag, index, detach = false))
  }

  /** Returns the changes accumulated since the last call to this function. */
  public fun takeChanges(): List<Change> {
    val result = changes.toList()
    this.changes.clear()

    for (id in removed) {
      val widget = widgets.remove(id)
        ?: throw IllegalStateException("Removed widget not present in map: $id")
      widget.depthFirstWalk(childrenRemover)
    }
    removed.clear()

    return result
  }

  override fun emitChanges() {
    if (changes.isNotEmpty()) {
      changesSink.sendChanges(takeChanges())
    }
  }

  private val childrenRemover: ProtocolWidget.ChildrenVisitor =
    object : ProtocolWidget.ChildrenVisitor {
      override fun visit(
        parent: ProtocolWidget,
        childrenTag: ChildrenTag,
        children: ProtocolWidgetChildren,
      ) {
        for (childWidget in children.widgets) {
          widgets.remove(childWidget.id.value)
        }
      }
    }
}
