/*
 * Copyright 2019 The Android Open Source Project
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

package app.cash.redwood

import androidx.compose.runtime.Stable

/**
 * An ordered, immutable collection of elements that change how a widget is laid out.
 */
@Stable
public interface LayoutModifier {

  /**
   * Accumulates a value starting with [initial] and applying [operation] to the current value
   * and each element from outside in.
   *
   * Elements wrap one another in a chain from left to right; an [Element] that appears to the
   * left of another in a `+` expression or in [operation]'s parameter order affects all
   * of the elements that appear after it. [foldIn] may be used to accumulate a value starting
   * from the parent or head of the modifier chain to the final wrapped child.
   */
  public fun <R> foldIn(initial: R, operation: (R, Element) -> R): R

  /**
   * Accumulates a value starting with [initial] and applying [operation] to the current value
   * and each element from inside out.
   *
   * Elements wrap one another in a chain from left to right; an [Element] that appears to the
   * left of another in a `+` expression or in [operation]'s parameter order affects all
   * of the elements that appear after it. [foldOut] may be used to accumulate a value starting
   * from the child or tail of the modifier chain up to the parent or head of the chain.
   */
  public fun <R> foldOut(initial: R, operation: (Element, R) -> R): R

  /**
   * Returns `true` if [predicate] returns true for any [Element] in this [LayoutModifier].
   */
  public fun any(predicate: (Element) -> Boolean): Boolean

  /**
   * Returns `true` if [predicate] returns true for all [Element]s in this [LayoutModifier] or if
   * this [LayoutModifier] contains no [Element]s.
   */
  public fun all(predicate: (Element) -> Boolean): Boolean

  /**
   * Concatenates this modifier with another.
   *
   * Returns a [LayoutModifier] representing this modifier followed by [other] in sequence.
   */
  public infix fun then(other: LayoutModifier): LayoutModifier =
    if (other === LayoutModifier) this else CombinedLayoutModifier(this, other)

  /**
   * A single element contained within a [LayoutModifier] chain.
   */
  public interface Element : LayoutModifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R =
      operation(initial, this)

    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R =
      operation(this, initial)

    override fun any(predicate: (Element) -> Boolean): Boolean = predicate(this)

    override fun all(predicate: (Element) -> Boolean): Boolean = predicate(this)
  }

  /**
   * The companion object `LayoutModifier` is the empty, default, or starter [LayoutModifier]
   * that contains no [elements][Element]. Use it to create a new [LayoutModifier] using
   * modifier extension factory functions or as the default value for [LayoutModifier] parameters.
   */
  // The companion object implements `LayoutModifier` so that it may be used as the start of a
  // modifier extension factory expression.
  public companion object : LayoutModifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = initial
    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R = initial
    override fun any(predicate: (Element) -> Boolean): Boolean = false
    override fun all(predicate: (Element) -> Boolean): Boolean = true
    override infix fun then(other: LayoutModifier): LayoutModifier = other
    override fun toString(): String = "LayoutModifier"
  }
}

/**
 * A node in a [LayoutModifier] chain. A CombinedModifier always contains at least two elements;
 * a LayoutModifier [outer] that wraps around the LayoutModifier [inner].
 */
public class CombinedLayoutModifier(
  private val outer: LayoutModifier,
  private val inner: LayoutModifier,
) : LayoutModifier {
  override fun <R> foldIn(initial: R, operation: (R, LayoutModifier.Element) -> R): R =
    inner.foldIn(outer.foldIn(initial, operation), operation)

  override fun <R> foldOut(initial: R, operation: (LayoutModifier.Element, R) -> R): R =
    outer.foldOut(inner.foldOut(initial, operation), operation)

  override fun any(predicate: (LayoutModifier.Element) -> Boolean): Boolean =
    outer.any(predicate) || inner.any(predicate)

  override fun all(predicate: (LayoutModifier.Element) -> Boolean): Boolean =
    outer.all(predicate) && inner.all(predicate)

  override fun equals(other: Any?): Boolean =
    other is CombinedLayoutModifier && outer == other.outer && inner == other.inner

  override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

  override fun toString(): String = "[" + foldIn("") { acc, element ->
    if (acc.isEmpty()) element.toString() else "$acc, $element"
  } + "]"
}
