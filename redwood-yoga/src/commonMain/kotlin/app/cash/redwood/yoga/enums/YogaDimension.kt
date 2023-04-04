/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.enums

enum class YogaDimension(private val mIntValue: Int) {
  WIDTH(0),
  HEIGHT(1);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaDimension {
      return when (value) {
        0 -> WIDTH
        1 -> HEIGHT
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
