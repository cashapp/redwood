/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaExperimentalFeature(private val mIntValue: Int) {
  WEB_FLEX_BASIS(0);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaExperimentalFeature {
      return when (value) {
        0 -> WEB_FLEX_BASIS
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
