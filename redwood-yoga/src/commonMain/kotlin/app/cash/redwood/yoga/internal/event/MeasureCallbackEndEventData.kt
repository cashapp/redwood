package app.cash.redwood.yoga.internal.event

import app.cash.redwood.yoga.internal.enums.YGMeasureMode

class MeasureCallbackEndEventData(
    val layoutContext: Any?,
    val width: Float,
    val widthMeasureMode: YGMeasureMode,
    val height: Float,
    val heightMeasureMode: YGMeasureMode,
    val measuredWidth: Float,
    val measuredHeight: Float,
    val reason: LayoutPassReason
) : CallableEvent() //Type originates from: event.h
