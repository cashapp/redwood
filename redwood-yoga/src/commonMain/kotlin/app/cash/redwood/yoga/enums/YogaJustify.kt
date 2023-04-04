/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaJustify(private val mIntValue: Int) {
    FLEX_START(0), CENTER(1), FLEX_END(2), SPACE_BETWEEN(3), SPACE_AROUND(4), SPACE_EVENLY(5);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaJustify {
            return when (value) {
                0 -> FLEX_START
                1 -> CENTER
                2 -> FLEX_END
                3 -> SPACE_BETWEEN
                4 -> SPACE_AROUND
                5 -> SPACE_EVENLY
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
