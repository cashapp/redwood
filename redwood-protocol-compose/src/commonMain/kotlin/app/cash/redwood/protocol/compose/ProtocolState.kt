/*
 * Copyright (C) 2022 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.cash.redwood.protocol.compose

import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.Id

public class ProtocolState : EventSink {
  private var nextId = Id.Root.value + 1U
  private val nodes = mutableMapOf<Id, DiffProducingWidget>()

  public fun nextId(): Id {
    val value = nextId
    nextId = value + 1U
    return Id(value)
  }

  public fun put(widget: DiffProducingWidget) {
    nodes[widget.id] = widget
  }

  public fun remove(id: Id) {
    nodes.remove(id)
  }

  override fun sendEvent(event: Event) {
    val node = checkNotNull(nodes[event.id]) {
      // TODO how to handle race where an incoming event targets this removed node?
      "Unknown node ${event.id} for event with tag ${event.tag}"
    }
    node.sendEvent(event)
  }
}
