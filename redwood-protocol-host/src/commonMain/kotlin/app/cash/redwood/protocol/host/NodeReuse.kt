/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.protocol.host

import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.WidgetTag
import app.cash.redwood.protocol.host.HostProtocolAdapter.ReuseNode

/**
 * Returns true if [a] and [b] have the same shape. The shapes match if:
 *
 *  * Both have the same widget tag
 *  * Both have the same number of children, in each slot
 *  * The paired up children have the same shape, recursively.
 *
 * Note that this condition isn't concerned with properties or modifiers.
 */
@OptIn(RedwoodCodegenApi::class)
internal fun shapesEqual(
  factory: GeneratedHostProtocol<*>,
  a: ReuseNode<*>,
  b: ProtocolNode<*>,
): Boolean {
  if (!a.eligibleForReuse) return false // This node is ineligible.
  if (a.widgetTag == UnknownWidgetTag) return false // No 'Create' for this.
  if (b.widgetTag != a.widgetTag) return false // Widget types don't match.

  val widgetChildren = factory.widget(a.widgetTag)
    ?.childrenTags
    ?: return true // Widget has no children.

  return widgetChildren.all { childrenTag ->
    val childrenTag = ChildrenTag(childrenTag)
    childrenEqual(
      factory = factory,
      aChildren = a.children,
      bChildren = b.children(childrenTag)?.nodes ?: listOf(),
      childrenTag = childrenTag,
    )
  }
}

/**
 * Note that [aChildren] contains all children with all tags, and [bChildren] only contains the
 * children with [childrenTag].
 */
@OptIn(RedwoodCodegenApi::class)
private fun childrenEqual(
  factory: GeneratedHostProtocol<*>,
  aChildren: List<ReuseNode<*>>,
  bChildren: List<ProtocolNode<*>>,
  childrenTag: ChildrenTag,
): Boolean {
  var aChildCount = 0

  for (a in aChildren) {
    if (a.childrenTag != childrenTag) continue // From a different slot.
    if (a.indexInParent != aChildCount) return false // Out of order child?
    if (aChildCount >= bChildren.size) return false // b has fewer children.
    val b = bChildren[aChildCount++]
    if (!shapesEqual(factory, a, b)) return false // Subtree mismatch.
  }

  if (aChildCount != bChildren.size) return false // b has more children.
  return true
}

/** Returns a hash of this node, or 0L if this node isn't eligible for reuse. */
@OptIn(RedwoodCodegenApi::class)
internal fun shapeHash(
  factory: GeneratedHostProtocol<*>,
  node: ReuseNode<*>,
): Long {
  if (!node.eligibleForReuse) return 0L // This node is ineligible.
  if (node.widgetTag == UnknownWidgetTag) return 0L // No 'Create' for this.

  var result = node.widgetTag.value.toLong()

  factory.widget(node.widgetTag)?.childrenTags?.forEach { childrenTag ->
    result = (result * 37L) + childrenTag
    var childCount = 0
    for (child in node.children) {
      if (child.childrenTag.value != childrenTag) continue
      if (child.indexInParent != childCount) return 0L // Out of order child?
      childCount++
      result = (result * 41L) + shapeHash(factory, child)
    }
  }

  return result
}

/** Returns the same hash as [shapeHash], but on an already-built [ProtocolNode]. */
@OptIn(RedwoodCodegenApi::class)
internal fun shapeHash(
  factory: GeneratedHostProtocol<*>,
  node: ProtocolNode<*>,
): Long {
  var result = node.widgetTag.value.toLong()
  factory.widget(node.widgetTag)?.childrenTags?.forEach { childrenTag ->
    result = (result * 37L) + childrenTag
    val children = node.children(ChildrenTag(childrenTag))
      ?: return@forEach // This acts like a 'continue'.
    for (child in children.nodes) {
      result = (result * 41L) + shapeHash(factory, child)
    }
  }

  return result
}

internal val UnknownWidgetTag: WidgetTag = WidgetTag(-1)
