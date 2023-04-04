/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
