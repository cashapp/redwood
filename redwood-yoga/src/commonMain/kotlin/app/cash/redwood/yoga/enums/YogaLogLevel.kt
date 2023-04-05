/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaLogLevel(private val mIntValue: Int) {
  ERROR(0),
  WARN(1),
  INFO(2),
  DEBUG(3),
  VERBOSE(4),
  FATAL(5);

  fun intValue(): Int {
    return mIntValue
  }

  companion object {
    fun fromInt(value: Int): YogaLogLevel {
      return when (value) {
        0 -> ERROR
        1 -> WARN
        2 -> INFO
        3 -> DEBUG
        4 -> VERBOSE
        5 -> FATAL
        else -> throw IllegalArgumentException("Unknown enum value: $value")
      }
    }
  }
}
