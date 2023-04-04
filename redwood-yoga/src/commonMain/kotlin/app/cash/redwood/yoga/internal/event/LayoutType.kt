package app.cash.redwood.yoga.internal.event

//struct YGConfig;
//struct YGNode;
enum class LayoutType //Type originates from: event.h
    (private val intValue: Int) {
    kLayout(0), kMeasure(1), kCachedLayout(2), kCachedMeasure(3);

    fun getValue(): Int {
        return intValue
    }

    companion object {

        private val mappings: Map<Int, LayoutType> = values().associateBy { it.intValue }
        private fun getMappings(): Map<Int, LayoutType> {
            return mappings
        }

        fun forValue(value: Int): LayoutType? {
            return getMappings()[value]
        }
    }
}
