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
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** @suppress For generated code use only. */
@RedwoodCodegenApi
public interface ProtocolState {

  public val json: Json

  /**
   * Host versions prior to 0.10.0 contained a bug where they did not recursively remove widgets
   * from the protocol map which leaked any child views of a removed node. We can work around this
   * on the guest side by synthesizing removes for every node in the subtree.
   */
  public val synthesizeSubtreeRemoval: Boolean

  public fun initChangesSink(changesSink: ChangesSink)

  public fun emitChanges()

  public fun nextId(): Id

  public fun appendCreate(
    id: Id,
    tag: WidgetTag,
  )

  public fun <T> appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  )

  public fun appendPropertyChange(
    id: Id,
    tag: PropertyTag,
    value: Boolean,
  )

  public fun appendModifierChange(
    id: Id,
    elements: List<ModifierElement>,
  )

  public fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    childId: Id,
    index: Int,
  )

  public fun appendMove(
    id: Id,
    tag: ChildrenTag,
    fromIndex: Int,
    toIndex: Int,
    count: Int,
  )

  public fun appendRemove(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    count: Int,
    removedIds: List<Id> = listOf(),
  )

  public fun addWidget(widget: ProtocolWidget)

  public fun removeWidget(id: Id)

  public fun getWidget(id: Id): ProtocolWidget?
}
