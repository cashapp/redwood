/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.internal

import kotlin.jvm.JvmInline

@JvmInline
internal value class YGFloatOptional(val value: Float = Float.NaN) {

  fun unwrap(): Float {
    return value
  }

  fun isUndefined(): Boolean {
    return value.isNaN()
  }
}
