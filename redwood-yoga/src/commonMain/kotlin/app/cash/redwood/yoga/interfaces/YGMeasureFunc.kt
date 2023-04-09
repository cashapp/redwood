package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.YGSize
import app.cash.redwood.yoga.enums.YGMeasureMode

fun interface YGMeasureFunc {
  operator fun invoke(
    node: YGNode,
    width: Float,
    widthMode: YGMeasureMode,
    height: Float,
    heightMode: YGMeasureMode,
  ): YGSize
}
