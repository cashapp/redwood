package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode

fun interface YGCloneNodeFunc {
  operator fun invoke(
    oldNode: YGNode,
    owner: YGNode?,
    childIndex: Int,
  ): YGNode
}
