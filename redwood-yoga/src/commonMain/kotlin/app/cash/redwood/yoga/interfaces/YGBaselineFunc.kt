package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode

fun interface YGBaselineFunc {
  operator fun invoke(
    node: YGNode,
    width: Float,
    height: Float,
  ): Float
}
