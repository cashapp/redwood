/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaWrap(private val mIntValue: Int) {
  NO_WRAP(0),
  WRAP(1),
  WRAP_REVERSE(2);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaWrap {
      return when (value) {
        0 -> NO_WRAP
        1 -> WRAP
        2 -> WRAP_REVERSE
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
