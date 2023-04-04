/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaDisplay(private val mIntValue: Int) {
    FLEX(0), NONE(1);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaDisplay {
            return when (value) {
                0 -> FLEX
                1 -> NONE
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
