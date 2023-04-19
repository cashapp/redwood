/*
 * Copyright (C) 2022 Square, Inc.
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
@file:Suppress("MemberVisibilityCanBePrivate")

package app.cash.redwood.layout.api

import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.serialization.Serializable

// TODO make value classes have private ctors and private vals.
//  Blocked by https://issuetracker.google.com/issues/251430194.

/** Controls how the container should determine its width/height. */
@[Immutable JvmInline Serializable]
public value class Constraint internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Wrap"
    1 -> "Fill"
    else -> throw AssertionError()
  }

  public companion object {
    public val Wrap: Constraint = Constraint(0)
    public val Fill: Constraint = Constraint(1)
  }
}

/** Equivalent to `justify-content`. */
@[Immutable JvmInline Serializable]
public value class MainAxisAlignment internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Start"
    1 -> "Center"
    2 -> "End"
    3 -> "SpaceBetween"
    4 -> "SpaceAround"
    5 -> "SpaceEvenly"
    else -> throw AssertionError()
  }

  public companion object {
    public val Start: MainAxisAlignment = MainAxisAlignment(0)
    public val Center: MainAxisAlignment = MainAxisAlignment(1)
    public val End: MainAxisAlignment = MainAxisAlignment(2)
    public val SpaceBetween: MainAxisAlignment = MainAxisAlignment(3)
    public val SpaceAround: MainAxisAlignment = MainAxisAlignment(4)
    public val SpaceEvenly: MainAxisAlignment = MainAxisAlignment(5)
  }
}

/** Equivalent to `align-items`. */
@[Immutable JvmInline Serializable]
public value class CrossAxisAlignment internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Start"
    1 -> "Center"
    2 -> "End"
    3 -> "Stretch"
    else -> throw AssertionError()
  }

  public companion object {
    public val Start: CrossAxisAlignment = CrossAxisAlignment(0)
    public val Center: CrossAxisAlignment = CrossAxisAlignment(1)
    public val End: CrossAxisAlignment = CrossAxisAlignment(2)
    public val Stretch: CrossAxisAlignment = CrossAxisAlignment(3)
  }
}

/** Equivalent to `overflow-x`/`overflow-y`. */
@[Immutable JvmInline Serializable]
public value class Overflow internal constructor(internal val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Clip"
    1 -> "Scroll"
    else -> throw AssertionError()
  }

  public companion object {
    public val Clip: Overflow = Overflow(0)
    public val Scroll: Overflow = Overflow(1)
  }
}
