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
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.widget.Widget
import kotlinx.serialization.json.JsonArray

/**
 * A [Widget] which consumes protocol diffs and applies them to a platform-specific representation.
 *
 * @suppress
 */
public abstract class DiffConsumingNode<W : Any>(
  public val parentId: Id,
  public val parentChildren: Widget.Children<W>,
) {
  private var _childIds: MutableList<Id>? = null
  public val childIds: MutableList<Id>
    get() = _childIds ?: mutableListOf<Id>().also { _childIds = it }

  public abstract val widget: Widget<W>

  public abstract fun apply(diff: PropertyDiff, eventSink: EventSink)

  public abstract fun updateLayoutModifier(value: JsonArray)

  /**
   * Return one of this widget's children groups by its [tag].
   *
   * Invalid [tag] values can either produce an exception or result in `null` being returned.
   * If `null` is returned, the caller should make every effort to ignore these children and
   * continue executing.
   */
  public abstract fun children(tag: ChildrenTag): Widget.Children<W>?

  public interface Factory<W : Any> {
    /**
     * Create a new protocol-consuming widget of the specified [tag].
     *
     * Invalid [tag] values can either produce an exception or result in `null` being returned.
     * If `null` is returned, the caller should make every effort to ignore this widget and
     * continue executing.
     */
    public fun create(
      parentId: Id,
      parentChildren: Widget.Children<W>,
      tag: WidgetTag,
    ): DiffConsumingNode<W>?
  }
}
