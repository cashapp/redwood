/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaLayoutType(private val mIntValue: Int) {
    LAYOUT(0), MEASURE(1), CACHED_LAYOUT(2), CACHED_MEASURE(3);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaLayoutType {
            return when (value) {
                0 -> LAYOUT
                1 -> MEASURE
                2 -> CACHED_LAYOUT
                3 -> CACHED_MEASURE
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
