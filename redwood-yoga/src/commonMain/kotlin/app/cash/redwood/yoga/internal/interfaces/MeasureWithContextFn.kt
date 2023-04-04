package app.cash.redwood.yoga.internal.interfaces

import app.cash.redwood.yoga.internal.YGNode
import app.cash.redwood.yoga.internal.YGSize
import app.cash.redwood.yoga.internal.enums.YGMeasureMode

fun interface MeasureWithContextFn {
    operator fun invoke(
      UnnamedParameter: YGNode?,
      UnnamedParameter2: Float,
      UnnamedParameter3: YGMeasureMode?,
      UnnamedParameter4: Float,
      UnnamedParameter5: YGMeasureMode?,
      UnnamedParameter6: Any?
    ): YGSize
}
