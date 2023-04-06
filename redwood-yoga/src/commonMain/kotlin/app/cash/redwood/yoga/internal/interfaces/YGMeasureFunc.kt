package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.YGSize
import app.cash.redwood.yoga.internal.enums.YGMeasureMode

fun interface YGMeasureFunc {
    operator fun invoke(
      node: YGNode,
      width: Float,
      widthMode: YGMeasureMode,
      height: Float,
      heightMode: YGMeasureMode
    ): YGSize
}
