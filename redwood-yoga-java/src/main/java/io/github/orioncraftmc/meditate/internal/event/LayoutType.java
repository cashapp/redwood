package io.github.orioncraftmc.meditate.internal.event;

//struct YGConfig;
//struct YGNode;


public enum LayoutType //Type originates from: event.h
{
    kLayout(0),
    kMeasure(1),
    kCachedLayout(2),
    kCachedMeasure(3);

    public static final int SIZE = java.lang.Integer.SIZE;
    private static java.util.HashMap<Integer, LayoutType> mappings;
    private final int intValue;

    LayoutType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, LayoutType> getMappings() {
        if (mappings == null) {
            synchronized (LayoutType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<>();
                }
            }
        }
        return mappings;
    }

    public static LayoutType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
