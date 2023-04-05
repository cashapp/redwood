/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
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
