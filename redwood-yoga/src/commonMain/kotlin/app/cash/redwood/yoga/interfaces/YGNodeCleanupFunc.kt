package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode

fun interface YGNodeCleanupFunc {
  operator fun invoke(node: YGNode)
}
