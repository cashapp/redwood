package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode

fun interface PrintWithContextFn {
    operator fun invoke(UnnamedParameter: YGNode?, UnnamedParameter2: Any?)
}
