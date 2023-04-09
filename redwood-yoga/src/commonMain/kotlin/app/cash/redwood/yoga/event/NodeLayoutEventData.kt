package app.cash.redwood.yoga.event

class NodeLayoutEventData(val layoutType: LayoutType, val layoutContext: Any?) :
    CallableEvent() //Type originates from: event.h
