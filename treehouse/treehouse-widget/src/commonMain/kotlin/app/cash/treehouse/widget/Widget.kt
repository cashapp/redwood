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
package app.cash.treehouse.widget

import app.cash.treehouse.protocol.Event
import app.cash.treehouse.protocol.PropertyDiff
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule

public interface Widget<T : Any> {
  public val value: T

  public fun apply(
    serializers: Map<Int, KSerializer<*>>,
    diff: PropertyDiff,
    events: (Event) -> Unit,
  )

  public fun children(tag: Int): Children<T> {
    throw IllegalArgumentException("Widget does not support children")
  }

  public fun createSerializers(module: SerializersModule): Map<Int, KSerializer<*>> = mapOf()

  /**
   * An interface for manipulating a widget's list of child widgets.
   *
   * Arguments to these methods can be assumed to be validated against the current state of the
   * list. No additional validation needs to be performed (for example, checking index bounds).
   */
  public interface Children<T : Any> {
    /** Insert child [widget] at [index]. */
    public fun insert(index: Int, widget: T)
    /**
     * Move [count] child widgets from [fromIndex] to [toIndex].
     *
     * Both indices are relative to the position before the change. For example, to move the
     * widget at position 1 to after the widget at position 2, [fromIndex] should be 1 and
     * [toIndex] should be 3. If the widgets were `A B C D E`, calling `move(1, 3, 1)` would
     * result in the widgets being reordered to `A C B D E`.
     */
    public fun move(fromIndex: Int, toIndex: Int, count: Int)
    /** Remove [count] child widgets starting from [index]. */
    public fun remove(index: Int, count: Int)
    /** Remove all child widgets. */
    public fun clear()
  }

  public interface Factory<T : Any> {
    public fun create(kind: Int, id: Long): Widget<T>
  }
}
