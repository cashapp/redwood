/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaAlign(private val mIntValue: Int) {
  AUTO(0),
  FLEX_START(1),
  CENTER(2),
  FLEX_END(3),
  STRETCH(4),
  BASELINE(5),
  SPACE_BETWEEN(6),
  SPACE_AROUND(7);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaAlign {
      return when (value) {
        0 -> AUTO
        1 -> FLEX_START
        2 -> CENTER
        3 -> FLEX_END
        4 -> STRETCH
        5 -> BASELINE
        6 -> SPACE_BETWEEN
        7 -> SPACE_AROUND
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
