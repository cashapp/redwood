/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.enums.YogaMeasureMode
import app.cash.redwood.yoga.YogaNode
import app.cash.redwood.yoga.internal.YGSize

interface YogaMeasureFunction {
    fun measure(
      node: YogaNode,
      width: Float,
      widthMode: YogaMeasureMode,
      height: Float,
      heightMode: YogaMeasureMode
    ): YGSize
}
