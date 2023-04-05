package app.cash.redwood.yoga.internal.enums

enum class YGEdge {
  YGEdgeLeft,
  YGEdgeTop,
  YGEdgeRight,
  YGEdgeBottom,
  YGEdgeStart,
  YGEdgeEnd,
  YGEdgeHorizontal,
  YGEdgeVertical,
  YGEdgeAll;

  fun getValue(): Int {
    return ordinal
  }

  companion object {
    fun forValue(value: Int): YGEdge {
      return values()[value]
    }
  }
}
