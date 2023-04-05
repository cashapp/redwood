/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaMeasureMode(private val mIntValue: Int) {
  UNDEFINED(0),
  EXACTLY(1),
  AT_MOST(2);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaMeasureMode {
      return when (value) {
        0 -> UNDEFINED
        1 -> EXACTLY
        2 -> AT_MOST
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
