package io.github.orioncraftmc.meditate.internal.event;

import io.github.orioncraftmc.meditate.internal.YGConfig;

public class NodeDeallocationEventData extends CallableEvent  //Type originates from: event.h
{
    public final YGConfig config;

    public NodeDeallocationEventData(YGConfig config) {
        this.config = config;
    }
}
