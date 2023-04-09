package io.github.orioncraftmc.meditate.internal.event;

import io.github.orioncraftmc.meditate.internal.YGConfig;

public class NodeAllocationEventData extends CallableEvent  //Type originates from: event.h
{
    public final YGConfig config;

    public NodeAllocationEventData(YGConfig config) {
        this.config = config;
    }
}
