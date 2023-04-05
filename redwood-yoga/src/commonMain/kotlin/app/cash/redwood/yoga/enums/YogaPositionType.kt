/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaPositionType(private val mIntValue: Int) {
  STATIC(0),
  RELATIVE(1),
  ABSOLUTE(2);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaPositionType {
      return when (value) {
        0 -> STATIC
        1 -> RELATIVE
        2 -> ABSOLUTE
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
