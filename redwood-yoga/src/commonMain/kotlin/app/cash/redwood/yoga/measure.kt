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
package app.cash.redwood.yoga

import app.cash.redwood.yoga.internal.Yoga
import dev.drewhamilton.poko.Poko
import kotlin.jvm.JvmInline

@RedwoodYogaApi
public fun interface MeasureCallback {
  public fun measure(
    node: Node,
    width: Float,
    widthMode: MeasureMode,
    height: Float,
    heightMode: MeasureMode,
  ): Size
}

@RedwoodYogaApi
@Poko public class Size(
  public val width: Float,
  public val height: Float,
) {
  public companion object {
    public const val UNDEFINED: Float = Yoga.YGUndefined
  }
}

@RedwoodYogaApi
@JvmInline
public value class MeasureMode private constructor(private val ordinal: Int) {

  override fun toString(): String = when (ordinal) {
    0 -> "Undefined"
    1 -> "Exactly"
    2 -> "AtMost"
    else -> throw AssertionError()
  }

  public companion object {
    public val Undefined: MeasureMode = MeasureMode(0)
    public val Exactly: MeasureMode = MeasureMode(1)
    public val AtMost: MeasureMode = MeasureMode(2)
  }
}
