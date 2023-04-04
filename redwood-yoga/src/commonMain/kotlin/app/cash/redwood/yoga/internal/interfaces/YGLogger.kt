package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGConfig
import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.enums.YGLogLevel

fun interface YGLogger {
    operator fun invoke(
      config: YGConfig?,
      node: YGNode?,
      level: YGLogLevel,
      format: String,
      vararg args: Any?
    ): Int
}
