/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaDirection(private val mIntValue: Int) {
  INHERIT(0),
  LTR(1),
  RTL(2);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaDirection {
      return when (value) {
        0 -> INHERIT
        1 -> LTR
        2 -> RTL
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
