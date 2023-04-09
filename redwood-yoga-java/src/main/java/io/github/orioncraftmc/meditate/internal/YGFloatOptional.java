package io.github.orioncraftmc.meditate.internal;

public class YGFloatOptional {
    private final float value_;

    public YGFloatOptional() {
        // TODO: qNaN
        value_ = Float.NaN;
    }

    public YGFloatOptional(float value) {
        this.value_ = value;
    }

    public final float unwrap() {
        return value_;
    }

    public final boolean isUndefined() {
        return Float.isNaN(value_);
    }

}
