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

package app.cash.redwood.flexbox

import kotlin.jvm.JvmInline

internal interface Axis {
  val min: Double
  val max: Double
}

internal fun Axis(min: Double, max: Double) = object : Axis {
  override val min get() = min
  override val max get() = max
}

@JvmInline
private value class Width(val constraints: Constraints): Axis {
  override val min get() = constraints.minWidth
  override val max get() = constraints.maxWidth
}

@JvmInline
private value class Height(val constraints: Constraints): Axis {
  override val min get() = constraints.minHeight
  override val max get() = constraints.maxHeight
}

internal fun Constraints.asWidth(): Axis = Width(this)

internal fun Constraints.asHeight(): Axis = Height(this)

internal val Axis.isFixed: Boolean
  get() = min == max

internal val Axis.isBounded: Boolean
  get() = max != Constraints.Infinity
