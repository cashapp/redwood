package app.cash.redwood.yoga.internal.enums

enum class YGFlexDirection {
    YGFlexDirectionColumn, YGFlexDirectionColumnReverse, YGFlexDirectionRow, YGFlexDirectionRowReverse;

    fun getValue(): Int {
        return ordinal
    }

    companion object {

        fun forValue(value: Int): YGFlexDirection {
            return values()[value]
        }
    }
}
