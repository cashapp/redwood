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
package app.cash.redwood.protocol.widget

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.Widget
import kotlin.native.ObjCName

/**
 * A node which consumes protocol changes and applies them to a platform-specific representation.
 *
 * @suppress
 */
@ObjCName("ProtocolNode", exact = true)
public interface ProtocolNode<W : Any> {
  public val widget: Widget<W>

  /**
   * Record that this node's [widget] has been inserted into [container].
   * Updates to this node's layout modifier will notify [container].
   * This function may only be invoked once on each instance.
   */
  public fun attachTo(container: Widget.Children<W>)

  public fun apply(change: PropertyChange, eventSink: EventSink)

  public fun updateModifier(elements: Array<ModifierElement>)

  /**
   * Return one of this node's children groups by its [tag].
   *
   * Invalid [tag] values can either produce an exception or result in `null` being returned.
   * If `null` is returned, the caller should make every effort to ignore these children and
   * continue executing.
   */
  public fun children(tag: ChildrenTag): Widget.Children<W>?

  @ObjCName("ProtocolNodeFactory", exact = true)
  public interface Factory<W : Any> {
    /**
     * Create a new protocol node of the specified [tag].
     *
     * Invalid [tag] values can either produce an exception or result in `null` being returned.
     * If `null` is returned, the caller should make every effort to ignore this node and
     * continue executing.
     */
    public fun create(tag: WidgetTag): ProtocolNode<W>?
  }
}
