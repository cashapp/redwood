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

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.widget.Widget

/**
 * A node which consumes protocol changes and applies them to a platform-specific representation.
 *
 * @suppress
 */
@RedwoodCodegenApi
public abstract class ProtocolNode<W : Any> {
  public abstract val widget: Widget<W>

  private var container: Widget.Children<W>? = null

  /**
   * Record that this node's [widget] has been inserted into [container].
   * Updates to this node's layout modifier will notify [container].
   * This function may only be invoked once on each instance.
   */
  public fun attachTo(container: Widget.Children<W>) {
    check(this.container == null)
    this.container = container
  }

  public abstract fun apply(change: PropertyChange, eventSink: EventSink)

  public fun updateModifier(modifier: Modifier) {
    widget.modifier = modifier
    container?.onModifierUpdated()
  }

  /**
   * Return one of this node's children groups by its [tag].
   *
   * Invalid [tag] values can either produce an exception or result in `null` being returned.
   * If `null` is returned, the caller should make every effort to ignore these children and
   * continue executing.
   */
  public abstract fun children(tag: ChildrenTag): Widget.Children<W>?
}
