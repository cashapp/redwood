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

import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifierChange
import app.cash.redwood.protocol.LayoutModifierElement
import app.cash.redwood.protocol.LayoutModifierTag
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import example.redwood.compose.ExampleSchemaProtocolBridge
import example.redwood.compose.TestScope
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.modules.SerializersModule

class GeneratedProtocolBridgeTest {
  @Test fun propertyUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ExampleSchemaProtocolBridge.create(json)
    val textInput = bridge.provider.ExampleSchema.TextInput()

    textInput.customType(10.seconds)

    val expected = listOf(
      Create(Id(1), WidgetTag(5)),
      PropertyChange(Id(1), PropertyTag(2), JsonPrimitive("PT10S")),
    )
    assertThat(bridge.getChangesOrNull()).isEqualTo(expected)
  }

  @Test fun layoutModifierUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ExampleSchemaProtocolBridge.create(json)
    val button = bridge.provider.ExampleSchema.Button()

    button.layoutModifiers = with(object : TestScope {}) {
      LayoutModifier.customType(10.seconds)
    }

    val expected = listOf(
      Create(Id(1), WidgetTag(4)),
      LayoutModifierChange(
        Id(1),
        listOf(
          LayoutModifierElement(
            LayoutModifierTag(3),
            buildJsonObject {
              put("customType", JsonPrimitive("PT10S"))
            },
          ),
        ),
      ),
    )
    assertThat(bridge.getChangesOrNull()).isEqualTo(expected)
  }

  @Test fun layoutModifierDefaultValueNotSerialized() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ExampleSchemaProtocolBridge.create(json)
    val button = bridge.provider.ExampleSchema.Button()

    button.layoutModifiers = with(object : TestScope {}) {
      LayoutModifier.customTypeWithDefault(10.seconds, "sup")
    }

    val expected = listOf(
      Create(Id(1), WidgetTag(4)),
      LayoutModifierChange(
        Id(1),
        listOf(
          LayoutModifierElement(
            LayoutModifierTag(5),
            buildJsonObject {
              put("customType", JsonPrimitive("PT10S"))
            },
          ),
        ),
      ),
    )
    assertThat(bridge.getChangesOrNull()).isEqualTo(expected)
  }

  @Test fun eventUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ExampleSchemaProtocolBridge.create(json)
    val textInput = bridge.provider.ExampleSchema.TextInput()

    val protocolWidget = textInput as ProtocolWidget

    var argument: Duration? = null
    textInput.onChangeCustomType {
      argument = it
    }

    protocolWidget.sendEvent(Event(Id(1), EventTag(4), listOf(JsonPrimitive("PT10S"))))

    assertThat(argument).isEqualTo(10.seconds)
  }

  @Test fun unknownEventThrowsDefault() {
    val bridge = ExampleSchemaProtocolBridge.create()
    val button = bridge.provider.ExampleSchema.Button() as ProtocolWidget

    val t = assertFailsWith<IllegalArgumentException> {
      button.sendEvent(Event(Id(1), EventTag(3456543)))
    }

    assertThat(t).hasMessage("Unknown event tag 3456543 for widget tag 4")
  }

  @Test fun unknownEventCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val bridge = ExampleSchemaProtocolBridge.create(mismatchHandler = handler)
    val button = bridge.provider.ExampleSchema.Button() as ProtocolWidget

    button.sendEvent(Event(Id(1), EventTag(3456543)))

    assertThat(handler.events.single()).isEqualTo("Unknown event 3456543 for 4")
  }

  @Test fun unknownEventNodeThrowsDefault() {
    val bridge = ExampleSchemaProtocolBridge.create()
    val t = assertFailsWith<IllegalArgumentException> {
      bridge.sendEvent(Event(Id(3456543), EventTag(1)))
    }
    assertThat(t).hasMessage("Unknown node ID 3456543 for event with tag 1")
  }

  @Test fun unknownEventNodeCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val bridge = ExampleSchemaProtocolBridge.create(mismatchHandler = handler)

    bridge.sendEvent(Event(Id(3456543), EventTag(1)))

    assertThat(handler.events.single()).isEqualTo("Unknown ID 3456543 for event tag 1")
  }
}
