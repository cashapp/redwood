package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGNode

fun interface YGDirtiedFunc {
  operator fun invoke(node: YGNode)
}
