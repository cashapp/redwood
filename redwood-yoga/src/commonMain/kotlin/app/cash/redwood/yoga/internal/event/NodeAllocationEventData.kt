package app.cash.redwood.yoga.internal.event

import app.cash.redwood.yoga.internal.YGConfig

class NodeAllocationEventData(val config: YGConfig?) :
    CallableEvent() //Type originates from: event.h
