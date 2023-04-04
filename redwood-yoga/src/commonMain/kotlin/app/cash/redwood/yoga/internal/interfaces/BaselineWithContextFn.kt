package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode

fun interface BaselineWithContextFn {
    operator fun invoke(
      UnnamedParameter: YGNode?,
      UnnamedParameter2: Float,
      UnnamedParameter3: Float,
      UnnamedParameter4: Any?
    ): Float
}
