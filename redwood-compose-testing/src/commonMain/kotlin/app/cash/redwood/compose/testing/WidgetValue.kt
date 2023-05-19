/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.compose.testing

import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ViewTree

/**
 * A widget that's implemented as a value class, appropriate for use in tests.
 *
 * Implementations of this interface may have lambda properties that trigger application behavior.
 * These lambda properties are **excluded** from [Any.equals], [Any.hashCode], and [Any.toString].
 */
public interface WidgetValue {
  public val layoutModifiers: LayoutModifier

  /** Returns all of the direct children of this widget, grouped by slot. */
  public val childrenLists: List<List<WidgetValue>>
    get() = listOf()

  public fun addTo(
    parentId: Id,
    childrenTag: ChildrenTag,
    childrenIndex: Int,
    builder: ViewTree.Builder,
  )
}

/**
 * Returns a sequence that does a depth-first preorder traversal of the entire widget tree whose
 * roots are this list. This is the same order elements occur in code.
 *
 * For example, given the following structure:
 *
 * ```
 * Column {
 *   Toolbar {
 *     Icon(...)
 *     Title(...)
 *   }
 *   Row {
 *     Text(...)
 *     Button(...)
 *   }
 * }
 * ```
 *
 * The flattened elements are returned in this order:
 *
 * ```
 * Column,
 * Toolbar,
 * Icon,
 * Title,
 * Row,
 * Text,
 * Button
 * ```
 */
public fun List<WidgetValue>.flatten(): Sequence<WidgetValue> {
  return sequence {
    for (widget in this@flatten) {
      flattenRecursive(widget)
    }
  }
}

private suspend fun SequenceScope<WidgetValue>.flattenRecursive(widgetValue: WidgetValue) {
  yield(widgetValue)
  for (children in widgetValue.childrenLists) {
    for (child in children) {
      flattenRecursive(child)
    }
  }
}
