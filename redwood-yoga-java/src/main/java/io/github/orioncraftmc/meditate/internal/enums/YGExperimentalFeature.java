package io.github.orioncraftmc.meditate.internal.enums;

public enum YGExperimentalFeature {
    YGExperimentalFeatureWebFlexBasis;

    public static final int SIZE = java.lang.Integer.SIZE;

    public static YGExperimentalFeature forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
