package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode

fun interface BaselineWithContextFn {
  operator fun invoke(
    node: YGNode,
    width: Float,
    height: Float,
    context: Any?,
  ): Float
}
