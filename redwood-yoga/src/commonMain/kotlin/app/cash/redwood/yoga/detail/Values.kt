package app.cash.redwood.yoga.detail

import app.cash.redwood.yoga.YGValue
import app.cash.redwood.yoga.enums.YGEdge

class Values<T> //Type originates from: Yoga-internal.h
{
    private val values_: MutableList<CompactValue>

    constructor() {
        values_ = ArrayList()
    }

    constructor(defaultValue: YGValue) {
        values_ = ArrayList()
        values_.add(CompactValue.createCompactValue(defaultValue))
    }

    constructor(defaultValue: YGValue, size: Int) {
        values_ = ArrayList(size)
        values_.add(CompactValue.createCompactValue(defaultValue))
    }

    private fun getValue(i: Int): CompactValue {
        while (values_.size < i + 1) {
            values_.add(CompactValue.ofUndefined())
        }
        return values_[i]
    }

    operator fun set(i: Int?, value: CompactValue) {
        values_[i!!] = value
    }

    fun getCompactValue(edge: YGEdge): CompactValue {
        return getCompactValue(edge.ordinal)
    }

    fun getCompactValue(i: Int): CompactValue {
        return CompactValue.createCompactValue(getValue(i).convertToYgValue())
    }

    operator fun get(i: Int): YGValue {
        return getValue(i).convertToYgValue()
    }

    operator fun set(i: Int, value: YGValue) {
        values_[i] = CompactValue.createCompactValue(value)
    }

    fun equalsTo(other: Values<T>): Boolean {
        for (i in values_.indices) {
            if (getValue(i) !== other.getValue(i)) {
                return false
            }
        }
        return true
    }
}
