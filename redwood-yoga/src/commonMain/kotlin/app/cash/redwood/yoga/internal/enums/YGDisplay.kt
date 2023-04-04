package app.cash.redwood.yoga.internal.enums

enum class YGDisplay {
    YGDisplayFlex, YGDisplayNone;

    fun getValue(): Int {
        return ordinal
    }

    companion object {
        fun forValue(value: Int): YGDisplay {
            return values()[value]
        }
    }
}
