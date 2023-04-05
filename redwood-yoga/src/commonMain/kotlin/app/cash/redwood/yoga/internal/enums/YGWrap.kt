package app.cash.redwood.yoga.internal.enums

enum class YGWrap //Type originates from: YGEnums.h
{
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
