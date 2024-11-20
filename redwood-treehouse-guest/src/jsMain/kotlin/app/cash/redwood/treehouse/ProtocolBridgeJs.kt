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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.Change
import app.cash.redwood.protocol.ChangesSink
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.RedwoodVersion
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.guest.GuestProtocolAdapter
import app.cash.redwood.protocol.guest.ProtocolMismatchHandler
import app.cash.redwood.protocol.guest.ProtocolWidget
import app.cash.redwood.protocol.guest.ProtocolWidgetChildren
import app.cash.redwood.protocol.guest.ProtocolWidgetSystemFactory
import app.cash.redwood.widget.WidgetSystem
import app.cash.zipline.asDynamicFunction
import app.cash.zipline.sourceType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToDynamic

internal actual fun GuestProtocolAdapter(
  json: Json,
  hostVersion: RedwoodVersion,
  widgetSystemFactory: ProtocolWidgetSystemFactory,
  mismatchHandler: ProtocolMismatchHandler,
): GuestProtocolAdapter = FastGuestProtocolAdapter(json, widgetSystemFactory, mismatchHandler)

@OptIn(ExperimentalSerializationApi::class, RedwoodCodegenApi::class)
internal class FastGuestProtocolAdapter(
  override val json: Json = Json.Default,
  private val widgetSystemFactory: ProtocolWidgetSystemFactory,
  private val mismatchHandler: ProtocolMismatchHandler = ProtocolMismatchHandler.Throwing,
) : GuestProtocolAdapter() {
  private var nextValue = Id.Root.value + 1
  private val widgets = JsMap<Int, ProtocolWidget>()
  private val changes = JsArray<Change>()
  private lateinit var changesSinkService: ChangesSinkService
  private lateinit var sendChanges: (service: ChangesSinkService, args: Array<*>) -> Any?

  override val widgetSystem: WidgetSystem<Unit> =
    widgetSystemFactory.create(this, mismatchHandler)

  override val root: ProtocolWidgetChildren =
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
    val changesSinkService = changesSink as ChangesSinkService
    initChangesSink(
      changesSinkService = changesSinkService,
      sendChanges = changesSinkService.sourceType!!.functions
        .single { "sendChanges" in it.signature }
        .asDynamicFunction(),
    )
  }

  internal fun initChangesSink(
    changesSinkService: ChangesSinkService,
    sendChanges: (service: ChangesSinkService, args: Array<*>) -> Any?,
  ) {
    this.changesSinkService = changesSinkService
    this.sendChanges = sendChanges
  }

  override fun nextId(): Id {
    val value = nextValue
    nextValue = value + 1
    return Id(value)
  }

  override fun appendCreate(
    id: Id,
    tag: WidgetTag,
  ) {
    val id = id
    val tag = tag
    changes.push(js("""["create",{"id":id,"tag":tag}]"""))
  }

  override fun <T> appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  ) {
    val id = id
    val widget = widgetTag
    val tag = propertyTag
    val encodedValue = value?.let { json.encodeToDynamic(serializer, it) }
    changes.push(js("""["property",{"id":id,"widget":widget,"tag":tag,"value":encodedValue}]"""))
  }

  override fun appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    value: Boolean,
  ) {
    val id = id
    val widget = widgetTag
    val tag = propertyTag
    val value = value
    changes.push(js("""["property",{"id":id,"widget":widget,"tag":tag,"value":value}]"""))
  }

  override fun appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    value: UInt,
  ) {
    val id = id
    val widget = widgetTag
    val tag = propertyTag
    val value = value.toDouble()
    changes.push(js("""["property",{"id":id,"widget":widget,"tag":tag,"value":value}]"""))
  }

  override fun appendModifierChange(id: Id, value: Modifier) {
    val elements = js("[]")

    value.forEach { element ->
      val (tag, serializer) = widgetSystemFactory.modifierTagAndSerializationStrategy(element)
      when {
        serializer != null -> {
          val value = json.encodeToDynamic(serializer, element)
          elements.push(js("""[tag,value]"""))
        }
        else -> {
          elements.push(js("""[tag]"""))
        }
      }
    }

    val id = id
    changes.push(js("""["modifier",{"id":id,"elements":elements}]"""))
  }

  override fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    child: ProtocolWidget,
  ) {
    check(!widgets.has(child.id.value)) {
      "Attempted to add widget with ID ${child.id} but one already exists"
    }
    widgets.set(child.id.value, child)

    val id = id
    val tag = tag
    val childId = child.id
    val index = index
    changes.push(js("""["add",{"id":id,"tag":tag,"childId":childId,"index":index}]"""))
  }

  override fun appendMove(
    id: Id,
    tag: ChildrenTag,
    fromIndex: Int,
    toIndex: Int,
    count: Int,
  ) {
    val id = id
    val tag = tag
    val fromIndex = fromIndex
    val toIndex = toIndex
    val count = count
    changes.push(js("""["move",{"id":id,"tag":tag,"fromIndex":fromIndex,"toIndex":toIndex,"count":count}]"""))
  }

  override fun appendRemove(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    count: Int,
    removedIds: List<Id>,
  ) {
    val id = id
    val tag = tag
    val index = index
    val count = count

    val removedIdsArray = js("[]")
    for (i in removedIds.indices) {
      removedIdsArray.push(removedIds[i].value)
    }

    changes.push(js("""["remove",{"id":id,"tag":tag,"index":index,"count":count,"removedIds":removedIdsArray}]"""))
  }

  override fun emitChanges() {
    if (changes.length > 0) {
      sendChanges(changesSinkService, arrayOf(changes))
      changes.clear()
    }
  }

  override fun removeWidget(id: Id) {
    widgets.delete(id.value)
  }
}
