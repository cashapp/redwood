package app.cash.redwood.yoga.internal.enums

enum class YGPrintOptions(private val value: Int) {
  YGPrintOptionsLayout(1),
  YGPrintOptionsStyle(2),
  YGPrintOptionsChildren(4);

  fun getValue(): Int {
    return value
  }

  companion object {
    fun forValue(value: Int): YGPrintOptions? {
      for (options in values()) {
        if (options.value == value) {
          return options
        }
      }
      return null
    }
  }
}
