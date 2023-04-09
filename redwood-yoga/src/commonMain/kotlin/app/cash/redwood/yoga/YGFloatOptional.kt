package app.cash.redwood.yoga

import kotlin.jvm.JvmInline

@JvmInline
value class YGFloatOptional(val value: Float = Float.NaN) {

    fun unwrap(): Float {
        return value
    }

    fun isUndefined(): Boolean {
        return value.isNaN()
    }
}
