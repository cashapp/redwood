package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGUnit

/** Type originates from: YGValue.h */
data class YGValue(
  var value: Float,
  val unit: YGUnit,
)
