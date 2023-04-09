package app.cash.redwood.yoga.enums

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
