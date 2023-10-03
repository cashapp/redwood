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
   * Iterates over all [Element]s in this [Modifier].
   */
  public fun forEach(block: (Element) -> Unit)

  /**
   * Concatenates this modifier with another.
   *
   * Returns a [Modifier] representing this modifier followed by [other] in sequence.
   */
  public infix fun then(other: Modifier): Modifier =
    if (other === Modifier) {
      this
    } else if (other is CombinedModifier) {
      var result: Modifier = this
      other.forEach { element -> result = result then element }
      result
    } else {
      CombinedModifier(this, other)
    }

  /**
   * A single element contained within a [Modifier] chain.
   */
  public interface Element : Modifier {
    override fun forEach(block: (Element) -> Unit): Unit = block(this)
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
    override fun forEach(block: (Element) -> Unit) {}
    override infix fun then(other: Modifier): Modifier = other
    override fun toString(): String = "Modifier"
  }
}

private class CombinedModifier(
  private val outer: Modifier,
  private val inner: Modifier,
) : Modifier {
  override fun forEach(block: (Modifier.Element) -> Unit) {
    outer.forEach(block)
    inner.forEach(block)
  }

  override fun equals(other: Any?): Boolean =
    other is CombinedModifier && outer == other.outer && inner == other.inner

  override fun hashCode(): Int = outer.hashCode() + 31 * inner.hashCode()

  override fun toString(): String = buildString {
    append('[')

    var first = true
    val appendElement: (Modifier.Element) -> Unit = { element ->
      if (!first) append(", ")
      first = false
      append(element)
    }
    outer.forEach(appendElement)
    inner.forEach(appendElement)

    append(']')
  }
}
