package app.cash.redwood.yoga.internal.enums

enum class YGDimension {
    YGDimensionWidth, YGDimensionHeight;

    fun getValue(): Int {
        return ordinal
    }

    companion object {
        fun forValue(value: Int): YGDimension {
            return values()[value]
        }
    }
}
