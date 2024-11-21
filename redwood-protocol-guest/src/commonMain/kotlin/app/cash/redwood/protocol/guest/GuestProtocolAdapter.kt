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

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChangesSink
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id
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
public abstract class GuestProtocolAdapter : EventSink {
  @RedwoodCodegenApi
  public abstract val json: Json

  /**
   * The provider of factories of widgets which record property changes and whose children changes
   * are also recorded. You **must** attach returned widgets to [root] or the children of a widget
   * in the tree beneath [root] in order for it to be tracked.
   */
  public abstract val widgetSystem: WidgetSystem<Unit>

  /**
   * The root of the widget tree onto which [widgetSystem]-produced widgets can be added. Changes to
   * this instance are recorded as changes to [Id.Root] and [ChildrenTag.Root].
   */
  public abstract val root: Widget.Children<Unit>

  public abstract fun initChangesSink(changesSink: ChangesSink)

  /**
   * Write changes to the underlying [ChangesSink].
   * This function may no-op if there are no changes to send.
   */
  public abstract fun emitChanges()

  @RedwoodCodegenApi
  public abstract fun nextId(): Id

  @RedwoodCodegenApi
  public abstract fun appendCreate(
    id: Id,
    tag: WidgetTag,
  )

  @RedwoodCodegenApi
  public abstract fun <T> appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    serializer: KSerializer<T>,
    value: T,
  )

  @RedwoodCodegenApi
  public abstract fun appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
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
  public abstract fun appendPropertyChange(
    id: Id,
    widgetTag: WidgetTag,
    propertyTag: PropertyTag,
    value: UInt,
  )

  @RedwoodCodegenApi
  public abstract fun appendModifierChange(
    id: Id,
    value: Modifier,
  )

  @RedwoodCodegenApi
  public abstract fun appendAdd(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    child: ProtocolWidget,
  )

  @RedwoodCodegenApi
  public abstract fun appendMove(
    id: Id,
    tag: ChildrenTag,
    fromIndex: Int,
    toIndex: Int,
    count: Int,
  )

  @RedwoodCodegenApi
  public abstract fun appendRemove(
    id: Id,
    tag: ChildrenTag,
    index: Int,
    count: Int,
  )

  @RedwoodCodegenApi
  public abstract fun removeWidget(id: Id)

  @RedwoodCodegenApi
  public val childrenRemover: ProtocolWidget.ChildrenVisitor =
    object : ProtocolWidget.ChildrenVisitor {
      override fun visit(
        parent: ProtocolWidget,
        childrenTag: ChildrenTag,
        children: ProtocolWidgetChildren,
      ) {
        for (childWidget in children.widgets) {
          removeWidget(childWidget.id)
        }
      }
    }
}
