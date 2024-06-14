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
import app.cash.redwood.protocol.ChangesSink
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.WidgetSystem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Connects the guest (composition) of a Redwood UI to the host of that UI.
 *
 * Guest widgets use this to send view tree updates to the host, and to receive user events from the
 * host.
 *
 * This interface is for generated code use only.
 */
public interface ProtocolGuest : EventSink {
  @RedwoodCodegenApi
  public val json: Json

  /**
   * Host versions prior to 0.10.0 contained a bug where they did not recursively remove widgets
   * from the protocol map which leaked any child views of a removed node. We can work around this
   * on the guest side by synthesizing removes for every node in the subtree.
   */
  @RedwoodCodegenApi
  public val synthesizeSubtreeRemoval: Boolean

  /**
   * The provider of factories of widgets which record property changes and whose children changes
   * are also recorded. You **must** attach returned widgets to [root] or the children of a widget
   * in the tree beneath [root] in order for it to be tracked.
   */
  public val widgetSystem: WidgetSystem<Unit>

  /**
   * The root of the widget tree onto which [widgetSystem]-produced widgets can be added. Changes to
   * this instance are recorded as changes to [Id.Root] and [ChildrenTag.Root].
   */
  public val root: Widget.Children<Unit>

  public fun initChangesSink(changesSink: ChangesSink)

  public fun emitChanges()

  @RedwoodCodegenApi
  public fun nextId(): Id

  @RedwoodCodegenApi
  public fun appendCreate(
    id: Id,
    tag: WidgetTag,
  )

  @RedwoodCodegenApi
  public fun <T> appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  )

  @RedwoodCodegenApi
  public fun appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    value: Boolean,
  )

  /**
   * There's a bug in kotlinx.serialization where decodeFromDynamic() is broken for UInt values
   * larger than MAX_INT. For example, 4294967295 is incorrectly encoded as -1. We work around that
   * here by special casing that type.
   *
   * https://github.com/Kotlin/kotlinx.serialization/issues/2713
   */
  @RedwoodCodegenApi
  public fun appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    value: UInt,
  )

  @RedwoodCodegenApi
  public fun appendModifierChange(
    id: Id,
    elements: List<ModifierElement>,
  )

  @RedwoodCodegenApi
  public fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    child: ProtocolWidget,
  )

  @RedwoodCodegenApi
  public fun appendMove(
    id: Id,
    tag: ChildrenTag,
    fromIndex: Int,
    toIndex: Int,
    count: Int,
  )

  @RedwoodCodegenApi
  public fun appendRemove(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    count: Int,
    removedIds: List<Id> = listOf(),
  )

  @RedwoodCodegenApi
  public fun removeWidget(id: Id)
}
