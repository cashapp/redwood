package app.cash.redwood.yoga.internal.enums

/** Type originates from: YGEnums.h */
enum class YGAlign {
  YGAlignAuto,
  YGAlignFlexStart,
  YGAlignCenter,
  YGAlignFlexEnd,
  YGAlignStretch,
  YGAlignBaseline,
  YGAlignSpaceBetween,
  YGAlignSpaceAround;

  fun getValue(): Int {
    return ordinal
  }

  companion object {
    fun forValue(value: Int): YGAlign {
      return values()[value]
    }
  }
}
