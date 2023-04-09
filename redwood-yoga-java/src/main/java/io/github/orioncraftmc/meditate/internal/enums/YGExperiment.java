package io.github.orioncraftmc.meditate.internal.enums;

import java.util.EnumSet;
import java.util.Set;

public enum YGExperiment {

    kDoubleMeasureCallbacks;

    private static final Set<YGExperiment> ENABLED_EXPERIMENTS = EnumSet.noneOf(YGExperiment.class);

    public static void disableAllExperiments() {
        ENABLED_EXPERIMENTS.clear();
    }

    public void enable() {
        ENABLED_EXPERIMENTS.add(this);
    }

    public void disable() {
        ENABLED_EXPERIMENTS.remove(this);
    }

    public boolean toggle() {
        if (ENABLED_EXPERIMENTS.contains(this)) {
            return ENABLED_EXPERIMENTS.remove(this);
        } else {
            ENABLED_EXPERIMENTS.add(this);
            return false;
        }
    }

    public boolean isEnabled() {
        return ENABLED_EXPERIMENTS.contains(this);
    }
}
