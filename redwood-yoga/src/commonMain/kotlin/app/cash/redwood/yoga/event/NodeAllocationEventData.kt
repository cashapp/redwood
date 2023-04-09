package app.cash.redwood.yoga.event

import app.cash.redwood.yoga.YGConfig

class NodeAllocationEventData(val config: YGConfig?) :
    CallableEvent() //Type originates from: event.h
