package io.github.orioncraftmc.meditate.internal.event;

import io.github.orioncraftmc.meditate.internal.enums.YGMeasureMode;

public class MeasureCallbackEndEventData extends CallableEvent  //Type originates from: event.h
{
    public final Object layoutContext;
    public final float width;
    public final YGMeasureMode widthMeasureMode;
    public final float height;
    public final YGMeasureMode heightMeasureMode;
    public final float measuredWidth;
    public final float measuredHeight;
    public final LayoutPassReason reason;

    public MeasureCallbackEndEventData(Object layoutContext, float width, YGMeasureMode widthMeasureMode, float height, YGMeasureMode heightMeasureMode, float measuredWidth, float measuredHeight, LayoutPassReason reason) {
        this.layoutContext = layoutContext;
        this.width = width;
        this.widthMeasureMode = widthMeasureMode;
        this.height = height;
        this.heightMeasureMode = heightMeasureMode;
        this.measuredWidth = measuredWidth;
        this.measuredHeight = measuredHeight;
        this.reason = reason;
    }
}
