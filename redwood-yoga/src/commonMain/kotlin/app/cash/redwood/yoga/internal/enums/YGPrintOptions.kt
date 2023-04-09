package app.cash.redwood.yoga.internal.enums

enum class YGPrintOptions {
  YGPrintOptionsLayout,
  YGPrintOptionsStyle,
  YGPrintOptionsChildren;

  fun getValue(): Int {
    return ordinal
  }

  companion object {
    fun forValue(value: Int): YGPrintOptions {
      return values()[value]
    }
  }
}
