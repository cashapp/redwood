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
package app.cash.redwood.protocol.guest

import app.cash.redwood.Modifier
import app.cash.redwood.protocol.Create
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierChange
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.example.redwood.testing.compose.TestScope
import com.example.redwood.testing.protocol.guest.TestSchemaProtocolBridge
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
    val bridge = TestSchemaProtocolBridge.create(json)
    val textInput = bridge.widgetSystem.TestSchema.TextInput()

    textInput.customType(10.seconds)

    val expected = listOf(
      Create(Id(1), WidgetTag(5)),
      PropertyChange(Id(1), PropertyTag(2), JsonPrimitive("PT10S")),
    )
    assertThat(bridge.getChangesOrNull()).isEqualTo(expected)
  }

  @Test fun modifierUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = TestSchemaProtocolBridge.create(json)
    val button = bridge.widgetSystem.TestSchema.Button()

    button.modifier = with(object : TestScope {}) {
      Modifier.customType(10.seconds)
    }

    val expected = listOf(
      Create(Id(1), WidgetTag(4)),
      ModifierChange(
        Id(1),
        listOf(
          ModifierElement(
            ModifierTag(3),
            buildJsonObject {
              put("customType", JsonPrimitive("PT10S"))
            },
          ),
        ),
      ),
    )
    assertThat(bridge.getChangesOrNull()).isEqualTo(expected)
  }

  @Test fun modifierDefaultValueNotSerialized() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = TestSchemaProtocolBridge.create(json)
    val button = bridge.widgetSystem.TestSchema.Button()

    button.modifier = with(object : TestScope {}) {
      Modifier.customTypeWithDefault(10.seconds, "sup")
    }

    val expected = listOf(
      Create(Id(1), WidgetTag(4)),
      ModifierChange(
        Id(1),
        listOf(
          ModifierElement(
            ModifierTag(5),
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
    val bridge = TestSchemaProtocolBridge.create(json)
    val textInput = bridge.widgetSystem.TestSchema.TextInput()

    val protocolWidget = textInput as ProtocolWidget

    var argument: Duration? = null
    textInput.onChangeCustomType {
      argument = it
    }

    protocolWidget.sendEvent(Event(Id(1), EventTag(4), listOf(JsonPrimitive("PT10S"))))

    assertThat(argument).isEqualTo(10.seconds)
  }

  @Test fun unknownEventThrowsDefault() {
    val bridge = TestSchemaProtocolBridge.create()
    val button = bridge.widgetSystem.TestSchema.Button() as ProtocolWidget

    val t = assertFailsWith<IllegalArgumentException> {
      button.sendEvent(Event(Id(1), EventTag(3456543)))
    }

    assertThat(t).hasMessage("Unknown event tag 3456543 for widget tag 4")
  }

  @Test fun unknownEventCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val bridge = TestSchemaProtocolBridge.create(mismatchHandler = handler)
    val button = bridge.widgetSystem.TestSchema.Button() as ProtocolWidget

    button.sendEvent(Event(Id(1), EventTag(3456543)))

    assertThat(handler.events.single()).isEqualTo("Unknown event 3456543 for 4")
  }

  @Test fun unknownEventNodeThrowsDefault() {
    val bridge = TestSchemaProtocolBridge.create()
    val t = assertFailsWith<IllegalArgumentException> {
      bridge.sendEvent(Event(Id(3456543), EventTag(1)))
    }
    assertThat(t).hasMessage("Unknown node ID 3456543 for event with tag 1")
  }

  @Test fun unknownEventNodeCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val bridge = TestSchemaProtocolBridge.create(mismatchHandler = handler)

    bridge.sendEvent(Event(Id(3456543), EventTag(1)))

    assertThat(handler.events.single()).isEqualTo("Unknown ID 3456543 for event tag 1")
  }
}
