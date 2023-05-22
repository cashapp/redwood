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
import kotlin.native.ObjCName

/**
 * An ordered, immutable collection of elements that change how a widget is laid out.
 */
@Stable
@ObjCName("Modifier", exact = true)
public interface Modifier {

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
   * Iterates over all [Element]s in this [Modifier].
   */
  public fun forEach(block: (Element) -> Unit)

  /**
   * Returns `true` if [predicate] returns true for any [Element] in this [Modifier].
   */
  public fun any(predicate: (Element) -> Boolean): Boolean

  /**
   * Returns `true` if [predicate] returns true for all [Element]s in this [Modifier] or if
   * this [Modifier] contains no [Element]s.
   */
  public fun all(predicate: (Element) -> Boolean): Boolean

  /**
   * Concatenates this modifier with another.
   *
   * Returns a [Modifier] representing this modifier followed by [other] in sequence.
   */
  public infix fun then(other: Modifier): Modifier =
    if (other === Modifier) this else CombinedModifier(this, other)

  /**
   * A single element contained within a [Modifier] chain.
   */
  public interface Element : Modifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R =
      operation(initial, this)

    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R =
      operation(this, initial)

    override fun forEach(block: (Element) -> Unit): Unit = block(this)

    override fun any(predicate: (Element) -> Boolean): Boolean = predicate(this)

    override fun all(predicate: (Element) -> Boolean): Boolean = predicate(this)
  }

  /**
   * The companion object `Modifier` is the empty, default, or starter [Modifier]
   * that contains no [elements][Element]. Use it to create a new [Modifier] using
   * modifier extension factory functions or as the default value for [Modifier] parameters.
   */
  // The companion object implements `Modifier` so that it may be used as the start of a
  // modifier extension factory expression.
  @ObjCName("EmptyModifier", exact = true)
  public companion object : Modifier {
    override fun <R> foldIn(initial: R, operation: (R, Element) -> R): R = initial
    override fun <R> foldOut(initial: R, operation: (Element, R) -> R): R = initial
    override fun forEach(block: (Element) -> Unit) {}
    override fun any(predicate: (Element) -> Boolean): Boolean = false
    override fun all(predicate: (Element) -> Boolean): Boolean = true
    override infix fun then(other: Modifier): Modifier = other
    override fun toString(): String = "Modifier"
  }
}

/**
 * A node in a [Modifier] chain. A CombinedModifier always contains at least two elements;
 * a Modifier [outer] that wraps around the Modifier [inner].
 */
@ObjCName("CombinedModifier", exact = true)
public class CombinedModifier(
  private val outer: Modifier,
  private val inner: Modifier,
) : Modifier {
  override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R =
    inner.foldIn(outer.foldIn(initial, operation), operation)

  override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R =
    outer.foldOut(inner.foldOut(initial, operation), operation)

  override fun forEach(block: (Modifier.Element) -> Unit) {
    outer.forEach(block)
    inner.forEach(block)
  }

  override fun any(predicate: (Modifier.Element) -> Boolean): Boolean =
    outer.any(predicate) || inner.any(predicate)

  override fun all(predicate: (Modifier.Element) -> Boolean): Boolean =
    outer.all(predicate) && inner.all(predicate)

  override fun equals(other: Any?): Boolean =
    other is CombinedModifier && outer == other.outer && inner == other.inner

  override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

  override fun toString(): String = "[" + foldIn("") { acc, element ->
    if (acc.isEmpty()) element.toString() else "$acc, $element"
  } + "]"
}
