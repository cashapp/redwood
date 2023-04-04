/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaOverflow(private val mIntValue: Int) {
    VISIBLE(0), HIDDEN(1), SCROLL(2);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaOverflow {
            return when (value) {
                0 -> VISIBLE
                1 -> HIDDEN
                2 -> SCROLL
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
