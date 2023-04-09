package app.cash.redwood.yoga.enums

enum class YGExperiment {
  kDoubleMeasureCallbacks;

  fun enable() {
    ENABLED_EXPERIMENTS.add(this)
  }

  fun disable() {
    ENABLED_EXPERIMENTS.remove(this)
  }

  fun toggle(): Boolean {
    return if (ENABLED_EXPERIMENTS.contains(this)) {
      ENABLED_EXPERIMENTS.remove(this)
    } else {
      ENABLED_EXPERIMENTS.add(this)
      false
    }
  }

  fun isEnabled(): Boolean {
    return ENABLED_EXPERIMENTS.contains(this)
  }

  companion object {
    private val ENABLED_EXPERIMENTS: MutableSet<YGExperiment> = mutableSetOf()

    fun disableAllExperiments() {
      ENABLED_EXPERIMENTS.clear()
    }
  }
}
