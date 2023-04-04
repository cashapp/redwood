package app.cash.redwood.yoga.internal.enums

enum class YGOverflow {
    YGOverflowVisible, YGOverflowHidden, YGOverflowScroll;

    fun getValue(): Int {
        return ordinal
    }

    companion object {
        fun forValue(value: Int): YGOverflow {
            return values()[value]
        }
    }
}
