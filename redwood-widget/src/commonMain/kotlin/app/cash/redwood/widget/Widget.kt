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
package app.cash.redwood.widget

import app.cash.redwood.LayoutModifier

public interface Widget<T : Any> {
  /**
   * The underlying platform-specific representation of this widget. This value will be supplied to
   * another widget's [Children] for display.
   */
  public val value: T

  /**
   * A collection of elements that change how a widget is laid out.
   */
  public var layoutModifiers: LayoutModifier

  /**
   * Marker interface for types whose functions create [Widget]s.
   */
  public interface Factory<T : Any>

  /**
   * An interface for manipulating a widget's list of children.
   *
   * Arguments to these methods can be assumed to be validated against the current state of the
   * list. No additional validation needs to be performed (for example, checking index bounds).
   */
  public interface Children<T : Any> {
    /** Insert child [widget] at [index]. */
    public fun insert(index: Int, widget: Widget<T>)

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

    /** Indicates [widget]'s [LayoutModifier] has been updated. */
    public fun updateLayoutModifier(widget: Widget<T>) {}
  }
}
