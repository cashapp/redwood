package io.github.orioncraftmc.meditate.internal.event;

public enum LayoutPassReason //Type originates from: event.h
{
    kInitial(0),
    kAbsLayout(1),
    kStretch(2),
    kMultilineStretch(3),
    kFlexLayout(4),
    kMeasureChild(5),
    kAbsMeasureChild(6),
    kFlexMeasure(7),
    COUNT(8);

    public static final int SIZE = java.lang.Integer.SIZE;
    private static java.util.HashMap<Integer, LayoutPassReason> mappings;
    private final int intValue;

    LayoutPassReason(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, LayoutPassReason> getMappings() {
        if (mappings == null) {
            synchronized (LayoutPassReason.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    public static LayoutPassReason forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
