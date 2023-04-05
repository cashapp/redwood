/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaFlexDirection(private val mIntValue: Int) {
  COLUMN(0),
  COLUMN_REVERSE(1),
  ROW(2),
  ROW_REVERSE(3);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaFlexDirection {
      return when (value) {
        0 -> COLUMN
        1 -> COLUMN_REVERSE
        2 -> ROW
        3 -> ROW_REVERSE
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
