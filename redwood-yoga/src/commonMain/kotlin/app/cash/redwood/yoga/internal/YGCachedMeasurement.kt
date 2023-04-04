package app.cash.redwood.yoga.internal

import app.cash.redwood.yoga.internal.enums.YGMeasureMode

class YGCachedMeasurement {
    var availableWidth: Float
    var availableHeight: Float
    var widthMeasureMode: YGMeasureMode
    var heightMeasureMode: YGMeasureMode
    var computedWidth: Float
    var computedHeight: Float

    init {
        availableWidth = -1f
        availableHeight = -1f
        widthMeasureMode = YGMeasureMode.YGMeasureModeUndefined
        heightMeasureMode = YGMeasureMode.YGMeasureModeUndefined
        computedWidth = -1f
        computedHeight = -1f
    }

    fun equalsTo(measurement: YGCachedMeasurement): Boolean {
        var isEqual =
            widthMeasureMode == measurement.widthMeasureMode && heightMeasureMode == measurement.heightMeasureMode
        if (!GlobalMembers.isUndefined(availableWidth) || !GlobalMembers.isUndefined(measurement.availableWidth)) {
            isEqual = isEqual && availableWidth == measurement.availableWidth
        }
        if (!GlobalMembers.isUndefined(availableHeight) || !GlobalMembers.isUndefined(measurement.availableHeight)) {
            isEqual = isEqual && availableHeight == measurement.availableHeight
        }
        if (!GlobalMembers.isUndefined(computedWidth) || !GlobalMembers.isUndefined(measurement.computedWidth)) {
            isEqual = isEqual && computedWidth == measurement.computedWidth
        }
        if (!GlobalMembers.isUndefined(computedHeight) || !GlobalMembers.isUndefined(measurement.computedHeight)) {
            isEqual = isEqual && computedHeight == measurement.computedHeight
        }
        return isEqual
    }
}
