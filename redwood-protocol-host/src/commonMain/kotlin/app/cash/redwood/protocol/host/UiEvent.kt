/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.protocol.host

import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

/**
 * A version of [Event] whose arguments have not yet been serialized to JSON and is thus
 * cheap to create on the UI thread.
 */
public interface UiEvent {
  /** Serialize this UI event into its protocol representation. */
  public fun toProtocol(): Event
}

/** A version of [EventSink] which consumes [UiEvent]s. */
public fun interface UiEventSink {
  public fun sendEvent(uiEvent: UiEvent)
}

/** @suppress For generated code use only. */
@RedwoodCodegenApi
public class GeneratedUiEvent(
  private val id: Id,
  private val tag: EventTag,
  private val json: Json?,
  private val args: Array<Any?>?,
  private val serializationStrategies: Array<out SerializationStrategy<Any?>>?,
) : UiEvent {
  override fun toProtocol(): Event {
    return Event(
      id = id,
      tag = tag,
      args = if (args == null) {
        emptyList()
      } else {
        val json = json!!
        val serializationStrategies = serializationStrategies!!
        List(args.size) { i ->
          json.encodeToJsonElement(serializationStrategies[i], args[i])
        }
      },
    )
  }
}
