package app.cash.redwood.yoga.internal.enums

enum class YGExperimentalFeature {
  YGExperimentalFeatureWebFlexBasis;

  fun getValue(): Int {
    return ordinal
  }

  companion object {
    fun forValue(value: Int): YGExperimentalFeature {
      return values()[value]
    }
  }
}
