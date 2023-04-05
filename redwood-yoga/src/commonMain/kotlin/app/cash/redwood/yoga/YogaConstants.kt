/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga

import app.cash.redwood.yoga.enums.YogaUnit

object YogaConstants {
    const val UNDEFINED = Float.NaN

    fun isUndefined(value: Float): Boolean {
        return value.isNaN()
    }

    fun isUndefined(value: YogaValue): Boolean {
        return value.unit == YogaUnit.UNDEFINED
    }

    fun getUndefined(): Float {
        return UNDEFINED
    }
}
