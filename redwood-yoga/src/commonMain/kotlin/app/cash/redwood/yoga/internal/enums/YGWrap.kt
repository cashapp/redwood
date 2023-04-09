package app.cash.redwood.yoga.internal.enums

/** Type originates from: YGEnums.h */
enum class YGWrap {
  YGWrapNoWrap,
  YGWrapWrap,
  YGWrapWrapReverse;

  fun getValue(): Int {
    return ordinal
  }

  companion object {
    fun forValue(value: Int): YGWrap {
      return values()[value]
    }
  }
}
