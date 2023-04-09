package app.cash.redwood.yoga.internal.enums

/** Type originates from: YGEnums.h */
enum class YGUnit {
  YGUnitUndefined,
  YGUnitPoint,
  YGUnitPercent,
  YGUnitAuto;

  fun getValue(): Int {
    return ordinal
  }

  companion object {
    fun forValue(value: Int): YGUnit {
      return values()[value]
    }
  }
}
