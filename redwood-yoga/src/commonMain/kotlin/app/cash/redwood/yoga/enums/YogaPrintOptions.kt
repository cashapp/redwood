/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaPrintOptions(private val mIntValue: Int) {
    LAYOUT(1), STYLE(2), CHILDREN(4);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaPrintOptions {
            return when (value) {
                1 -> LAYOUT
                2 -> STYLE
                4 -> CHILDREN
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
