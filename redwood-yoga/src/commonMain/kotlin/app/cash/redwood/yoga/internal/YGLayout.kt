package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGDirection

class YGLayout {
    private val flags: MutableMap<Any?, Any> = HashMap()
    val position = createEmptyFloatArray()
    val dimensions = ArrayList(listOf(GlobalMembers.YGUndefined, GlobalMembers.YGUndefined))
    val margin = createEmptyFloatArray()
    val border = createEmptyFloatArray()
    val padding = createEmptyFloatArray()
    var computedFlexBasisGeneration = 0
    var computedFlexBasis = YGFloatOptional()
    var generationCount = 0
    var lastOwnerDirection = YGDirection.YGDirectionInherit
    var nextCachedMeasurementsIndex = 0
    val cachedMeasurements = ArrayList<YGCachedMeasurement>(YG_MAX_CACHED_RESULT_COUNT)
    val measuredDimensions =
        ArrayList(listOf(GlobalMembers.YGUndefined, GlobalMembers.YGUndefined))
    val cachedLayout = YGCachedMeasurement()

    init {
        for (i in 0 until YG_MAX_CACHED_RESULT_COUNT) {
            cachedMeasurements.add(YGCachedMeasurement())
        }
    }

    private fun createEmptyFloatArray(): ArrayList<Float> {
        return ArrayList(listOf(0f, 0f, 0f, 0f))
    }

    fun direction(): YGDirection {
        return app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.getEnumData<YGDirection>(
            YGDirection::class, flags, directionOffset
        )
    }

    fun setDirection(direction: YGDirection) {
        app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.setEnumData<YGDirection>(
            YGDirection::class, flags, directionOffset, direction
        )
    }

    fun didUseLegacyFlag(): Boolean {
        return app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.getBooleanData(
            flags,
            didUseLegacyFlagOffset
        )
    }

    fun setDidUseLegacyFlag(`val`: Boolean) {
        app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.setBooleanData(
            flags,
            didUseLegacyFlagOffset,
            `val`
        )
    }

    fun doesLegacyStretchFlagAffectsLayout(): Boolean {
        return app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.getBooleanData(
            flags,
            doesLegacyStretchFlagAffectsLayoutOffset
        )
    }

    fun setDoesLegacyStretchFlagAffectsLayout(`val`: Boolean) {
        app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.setBooleanData(
            flags,
            doesLegacyStretchFlagAffectsLayoutOffset,
            `val`
        )
    }

    fun hadOverflow(): Boolean {
        return app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.getBooleanData(
            flags,
            hadOverflowOffset
        )
    }

    fun setHadOverflow(hadOverflow: Boolean) {
        app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.setBooleanData(
            flags,
            hadOverflowOffset,
            hadOverflow
        )
    }

    fun equalsTo(layout: YGLayout): Boolean //Method definition originates from: YGLayout.cpp
    {
        var isEqual = GlobalMembers.YGFloatArrayEqual(
          position,
          layout.position
        ) && GlobalMembers.YGFloatArrayEqual(
          dimensions,
          layout.dimensions
        ) && GlobalMembers.YGFloatArrayEqual(
          margin,
          layout.margin
        ) && GlobalMembers.YGFloatArrayEqual(
          border,
          layout.border
        ) && GlobalMembers.YGFloatArrayEqual(
          padding,
          layout.padding
        ) && direction() == layout.direction() && hadOverflow() == layout.hadOverflow() && lastOwnerDirection == layout.lastOwnerDirection && nextCachedMeasurementsIndex == layout.nextCachedMeasurementsIndex && cachedLayout.equalsTo(
            layout.cachedLayout
        ) && computedFlexBasis === layout.computedFlexBasis
        var i = 0
        while (i < YG_MAX_CACHED_RESULT_COUNT && isEqual) {
            //TODO: Verify if this is correct
            isEqual = cachedMeasurements[i].equalsTo(layout.cachedMeasurements[i])
            ++i
        }
        if (!GlobalMembers.isUndefined(measuredDimensions[0]) || !GlobalMembers.isUndefined(
            layout.measuredDimensions[0]
          )
        ) {
            isEqual = isEqual && measuredDimensions[0] == layout.measuredDimensions[0]
        }
        if (!GlobalMembers.isUndefined(measuredDimensions[1]) || !GlobalMembers.isUndefined(
            layout.measuredDimensions[1]
          )
        ) {
            isEqual = isEqual && measuredDimensions[1] == layout.measuredDimensions[1]
        }
        return isEqual
    }

    fun notEqualsTo(layout: YGLayout): Boolean {
        return !equalsTo(layout)
    }

    companion object {
        // This value was chosen based on empirical data:
        // 98% of analyzed layouts require less than 8 entries.
        const val YG_MAX_CACHED_RESULT_COUNT = 8
        private const val directionOffset = 0
        private val didUseLegacyFlagOffset: Int =
            directionOffset + app.cash.redwood.yoga.internal.detail.GlobalMembers.Companion.bitWidthFn<YGDirection>()
        private val doesLegacyStretchFlagAffectsLayoutOffset = didUseLegacyFlagOffset + 1
        private val hadOverflowOffset = doesLegacyStretchFlagAffectsLayoutOffset + 1
    }
}
