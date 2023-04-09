package io.github.orioncraftmc.meditate.internal.enums;

public enum YGUnit //Type originates from: YGEnums.h
{
    YGUnitUndefined,
    YGUnitPoint,
    YGUnitPercent,
    YGUnitAuto;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGUnit forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
