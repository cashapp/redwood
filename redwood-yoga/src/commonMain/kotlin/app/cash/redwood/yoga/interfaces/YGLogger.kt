package app.cash.redwood.yoga.interfaces

import app.cash.redwood.yoga.YGConfig
import app.cash.redwood.yoga.YGNode
import app.cash.redwood.yoga.enums.YGLogLevel

fun interface YGLogger {
  operator fun invoke(
    config: YGConfig?,
    node: YGNode?,
    level: YGLogLevel,
    format: String,
    vararg args: Any?,
  ): Int
}
