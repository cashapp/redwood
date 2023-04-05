/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaUnit(private val mIntValue: Int) {
  UNDEFINED(0),
  POINT(1),
  PERCENT(2),
  AUTO(3);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaUnit {
      return when (value) {
        0 -> UNDEFINED
        1 -> POINT
        2 -> PERCENT
        3 -> AUTO
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
