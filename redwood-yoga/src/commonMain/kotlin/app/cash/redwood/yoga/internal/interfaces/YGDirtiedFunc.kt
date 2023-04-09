package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode

fun interface YGDirtiedFunc {
  operator fun invoke(node: YGNode)
}
