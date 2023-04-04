/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.enums

enum class YogaNodeType(private val mIntValue: Int) {
    DEFAULT(0), TEXT(1);

    fun intValue(): Int {
        return mIntValue
    }

    companion object {
        fun fromInt(value: Int): YogaNodeType {
            return when (value) {
                0 -> DEFAULT
                1 -> TEXT
                else -> throw IllegalArgumentException("Unknown enum value: $value")
            }
        }
    }
}
