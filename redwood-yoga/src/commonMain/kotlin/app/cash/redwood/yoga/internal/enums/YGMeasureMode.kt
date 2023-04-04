package app.cash.redwood.yoga.internal.enums

enum class YGMeasureMode {
    YGMeasureModeUndefined, YGMeasureModeExactly, YGMeasureModeAtMost;

    fun getValue(): Int {
        return ordinal
    }

    companion object {
        fun forValue(value: Int): YGMeasureMode {
            return values()[value]
        }
    }
}
