/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YogaUnit

data class YogaValue(val value: Float, val unit: YogaUnit) {

    override fun toString(): String {
        return when (unit) {
            YogaUnit.UNDEFINED -> "undefined"
            YogaUnit.POINT -> value.toString()
            YogaUnit.PERCENT -> "$value%"
            YogaUnit.AUTO -> "auto"
        }
    }

    companion object {
        val UNDEFINED = YogaValue(YogaConstants.UNDEFINED, YogaUnit.UNDEFINED)
        val ZERO: YogaValue = YogaValue(0f, YogaUnit.POINT)
        val AUTO = YogaValue(YogaConstants.UNDEFINED, YogaUnit.AUTO)
        fun parse(s: String?): YogaValue? {
            if (s == null) {
                return null
            }
            if ("undefined" == s) {
                return UNDEFINED
            }
            if ("auto" == s) {
                return AUTO
            }
            return if (s.endsWith("%")) {
                YogaValue(s.substring(0, s.length - 1).toFloat(), YogaUnit.PERCENT)
            } else YogaValue(s.toFloat(), YogaUnit.POINT)
        }
    }
}
