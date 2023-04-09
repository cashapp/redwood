package app.cash.redwood.yoga.event

class LayoutData : CallableEvent() //Type originates from: event.h
{
    var layouts = 0
    var measures = 0
    var maxMeasureCache = 0
    var cachedLayouts = 0
    var cachedMeasures = 0
    var measureCallbacks = 0
    val measureCallbackReasonsCount = mutableListOf<Int?>()

    init {
        for (i in 0 until LayoutPassReason.COUNT.getValue()) {
            measureCallbackReasonsCount.add(0)
        }
    }
}
