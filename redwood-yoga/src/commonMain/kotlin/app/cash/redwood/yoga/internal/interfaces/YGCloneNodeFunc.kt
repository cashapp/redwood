package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode

fun interface YGCloneNodeFunc {
    operator fun invoke(oldNode: YGNode?, owner: YGNode?, childIndex: Int): YGNode
}
