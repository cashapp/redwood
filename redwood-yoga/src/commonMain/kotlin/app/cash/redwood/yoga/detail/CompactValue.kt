package app.cash.redwood.yoga.detail

import app.cash.redwood.yoga.YGValue
import app.cash.redwood.yoga.enums.YGUnit
import kotlin.math.abs

//static_assert(std::numeric_limits<float>::is_iec559, "facebook::yoga::detail::CompactValue only works with IEEE754 floats");
class CompactValue //Type originates from: CompactValue.h
{
    private val payload_: Payload
    private var undefined = false

    constructor() {
        undefined = true
        payload_ = Payload(Float.NaN, YGUnit.YGUnitUndefined)
    }

    private constructor(data: Payload) {
        payload_ = data
    }

    fun convertToYgValue(): YGValue {
        return YGValue(payload_.value, payload_.unit)
    }

    fun isUndefined(): Boolean {
        return undefined || !isAuto() && !isPoint() && !isPercent() &&
            payload_.value.isNaN()
    }

    private fun isPercent(): Boolean {
        return payload_.unit == YGUnit.YGUnitPercent
    }

    private fun isPoint(): Boolean {
        return payload_.unit == YGUnit.YGUnitPoint
    }

    fun isAuto(): Boolean {
        return payload_.unit == YGUnit.YGUnitAuto
    }

    private fun repr(): YGUnit {
        return payload_.unit
    }

    internal class Payload //Type originates from: CompactValue.h
        (val value: Float, val unit: YGUnit)

    companion object {
        const val LOWER_BOUND = 1.08420217e-19f
        const val UPPER_BOUND_POINT = 36893485948395847680.0f
        const val UPPER_BOUND_PERCENT = 18446742974197923840.0f
        fun of(value: Float, Unit: YGUnit): CompactValue {
            var value = value
            if (value < LOWER_BOUND && value > -LOWER_BOUND) {
                return CompactValue(Payload(0f, Unit))
            }
            val upperBound =
                if (Unit == YGUnit.YGUnitPercent) UPPER_BOUND_PERCENT else UPPER_BOUND_POINT
            if (value > upperBound || value < -upperBound) {
                value = abs(upperBound) * if (value < 0) -1 else 1
            }
            val data = Payload(value, Unit)
            return CompactValue(data)
        }

        fun ofMaybe(value: Float, Unit: YGUnit): CompactValue {
            return if (value.isNaN() || value.isInfinite()) ofUndefined() else of(
                value,
                Unit
            )
        }

        fun ofZero(): CompactValue {
            return CompactValue(Payload(0f, YGUnit.YGUnitPoint))
        }

        fun ofUndefined(): CompactValue {
            return CompactValue()
        }

        fun ofAuto(): CompactValue {
            return CompactValue(Payload(0f, YGUnit.YGUnitAuto))
        }

        fun createCompactValue(x: YGValue): CompactValue {
            val compactValue = when (x.unit) {
                YGUnit.YGUnitUndefined -> ofUndefined()
                YGUnit.YGUnitAuto -> ofAuto()
                YGUnit.YGUnitPoint -> of(x.value, YGUnit.YGUnitPoint)
                YGUnit.YGUnitPercent -> of(x.value, YGUnit.YGUnitPercent)
            }
            return compactValue
        }

        fun equalsTo(
            a: CompactValue,
            b: CompactValue
        ): Boolean //Method definition originates from: CompactValue.h
        {
            return a.payload_.unit == b.payload_.unit
        }
    }
}
