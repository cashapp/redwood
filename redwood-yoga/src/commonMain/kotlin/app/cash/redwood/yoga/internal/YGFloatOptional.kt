package app.cash.redwood.yoga.internal

class YGFloatOptional {
    private val value_: Float

    constructor() {
        // TODO: qNaN
        value_ = Float.NaN
    }

    constructor(value: Float) {
        value_ = value
    }

    fun unwrap(): Float {
        return value_
    }

    fun isUndefined(): Boolean {
        return value_.isNaN()
    }
}
