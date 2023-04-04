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

import app.cash.redwood.yoga.internal.YGSize
import kotlin.jvm.JvmStatic

/**
 * Helpers for building measure output value.
 */
object YogaMeasureOutput {
  @JvmStatic
  fun make(width: Float, height: Float): YGSize {
    return YGSize(width, height)
  }

  @JvmStatic
  fun make(width: Int, height: Int): YGSize {
    return make(width.toFloat(), height.toFloat())
  }

  @JvmStatic
  fun getWidth(measureOutput: YGSize): Float {
    return measureOutput.width
  }

  @JvmStatic
  fun getHeight(measureOutput: YGSize): Float {
    return measureOutput.height
  }
}
