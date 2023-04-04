/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaEdge(private val mIntValue: Int) {
    LEFT(0), TOP(1), RIGHT(2), BOTTOM(3), START(4), END(5), HORIZONTAL(6), VERTICAL(7), ALL(8);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaEdge {
            return when (value) {
                0 -> LEFT
                1 -> TOP
                2 -> RIGHT
                3 -> BOTTOM
                4 -> START
                5 -> END
                6 -> HORIZONTAL
                7 -> VERTICAL
                8 -> ALL
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
