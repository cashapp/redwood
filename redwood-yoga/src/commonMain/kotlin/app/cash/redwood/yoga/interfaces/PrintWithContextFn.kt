package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode

fun interface PrintWithContextFn {
  operator fun invoke(
    node: YGNode,
    context: Any?,
  )
}
