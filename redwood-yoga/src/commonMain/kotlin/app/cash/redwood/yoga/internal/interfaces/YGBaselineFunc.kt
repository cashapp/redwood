package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode

fun interface YGBaselineFunc {
  operator fun invoke(
    node: YGNode,
    width: Float,
    height: Float,
  ): Float
}
