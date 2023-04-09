package io.github.orioncraftmc.meditate.internal.event;

public class NodeLayoutEventData extends CallableEvent  //Type originates from: event.h
{
    public final LayoutType layoutType;
    public final Object layoutContext;

    public NodeLayoutEventData(LayoutType layoutType, Object layoutContext) {
        this.layoutType = layoutType;
        this.layoutContext = layoutContext;
    }
}
