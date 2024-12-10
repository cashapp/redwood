/*
 * Copyright (C) 2021 Square, Inc.
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
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.Widget

/**
 * A [Widget] with no platform-specific representation which instead produces protocol changes
 * based on its properties.
 *
 * @suppress For generated code use only.
 */
@RedwoodCodegenApi
public interface ProtocolWidget : Widget<Unit> {
  public val id: Id
  public val tag: WidgetTag

  /**
   * Used to detect when a removed widget is re-added later in the frame, such as when
   * `movableContentOf` is used. When a widget is added with this value greater than or equal to 0,
   * the original 'remove' operation should be changed to indicate that this node
   * (and its entire subtree) should be retained host-side, as a future 'add' of this node
   * will occur without associated 'create' events for the subtree.
   */
  public var removeIndex: Int

  override val value: Unit get() = Unit

  public fun sendEvent(event: Event)

  /**
   * Perform a depth-first walk of this widget's children hierarchy.
   *
   * For example, given the hierarchy:
   * ```kotlin
   * Split(
   *   left = {
   *     Row {
   *       Text(..)
   *       Button(..)
   *     }
   *   },
   *   right = {
   *     Column {
   *       Button(..)
   *       Text(..)
   *     }
   *   }
   * }
   * ```
   * You will see the following argument values passed to [visitor] if invoked on the `Split`:
   * 1. parent: `Row`, childrenTag: 1, children: `[Text+Button]`
   * 2. parent: `Split`, childrenTag: 1, children: `[Row]`
   * 3. parent: `Column`, childrenTag: 1, children: `[Button+Text]`
   * 4. parent: `Split`, childrenTag: 2, children: `[Column]`
   */
  public fun depthFirstWalk(visitor: ChildrenVisitor)

  @RedwoodCodegenApi // https://github.com/Kotlin/binary-compatibility-validator/issues/91
  public fun interface ChildrenVisitor {
    public fun visit(
      parent: ProtocolWidget,
      childrenTag: ChildrenTag,
      children: ProtocolWidgetChildren,
    )
  }

  @RedwoodCodegenApi // https://github.com/Kotlin/binary-compatibility-validator/issues/91
  public companion object {
    public const val INVALID_INDEX: Int = -1
  }
}
