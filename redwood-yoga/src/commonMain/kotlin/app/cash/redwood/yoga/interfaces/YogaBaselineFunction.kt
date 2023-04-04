/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YogaNode

fun interface YogaBaselineFunction {
    /**
     * Return the baseline of the node in points. When no baseline function is set the baseline
     * default to the computed height of the node.
     */
    fun baseline(node: YogaNode?, width: Float, height: Float): Float
}
