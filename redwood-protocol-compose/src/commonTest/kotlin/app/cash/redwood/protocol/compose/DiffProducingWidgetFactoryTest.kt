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
import app.cash.redwood.protocol.Diff
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.LayoutModifiers
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.PropertyTag
import example.redwood.compose.DiffProducingExampleSchemaWidgetFactory
import example.redwood.compose.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.modules.SerializersModule

class DiffProducingWidgetFactoryTest {
  @Test fun propertyUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ProtocolBridge()
    val factory = DiffProducingExampleSchemaWidgetFactory(bridge, json)
    val textInput = factory.TextInput()

    textInput.customType(10.seconds)

    val expected = Diff(
      propertyDiffs = listOf(
        PropertyDiff(Id(1), PropertyTag(2), JsonPrimitive("PT10S")),
      ),
    )
    assertEquals(expected, bridge.createDiffOrNull())
  }

  @Test fun layoutModifierUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ProtocolBridge()
    val factory = DiffProducingExampleSchemaWidgetFactory(bridge, json)
    val button = factory.Button()

    button.layoutModifiers = with(object : TestScope {}) {
      LayoutModifier.customType(10.seconds)
    }

    val expected = Diff(
      layoutModifiers = listOf(
        LayoutModifiers(
          Id(1),
          buildJsonArray {
            add(
              buildJsonArray {
                add(JsonPrimitive(3))
                add(
                  buildJsonObject {
                    put("customType", JsonPrimitive("PT10S"))
                  },
                )
              },
            )
          },
        ),
      ),
    )
    assertEquals(expected, bridge.createDiffOrNull())
  }

  @Test fun layoutModifierDefaultValueNotSerialized() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val bridge = ProtocolBridge()
    val factory = DiffProducingExampleSchemaWidgetFactory(bridge, json)
    val button = factory.Button()

    button.layoutModifiers = with(object : TestScope {}) {
      LayoutModifier.customTypeWithDefault(10.seconds, "sup")
    }

    val expected = Diff(
      layoutModifiers = listOf(
        LayoutModifiers(
          Id(1),
          buildJsonArray {
            add(
              buildJsonArray {
                add(JsonPrimitive(5))
                add(
                  buildJsonObject {
                    put("customType", JsonPrimitive("PT10S"))
                  },
                )
              },
            )
          },
        ),
      ),
    )
    assertEquals(expected, bridge.createDiffOrNull())
  }

  @Test fun eventUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val factory = DiffProducingExampleSchemaWidgetFactory(ProtocolBridge(), json)
    val textInput = factory.TextInput()

    val diffProducingWidget = textInput as DiffProducingWidget

    var argument: Duration? = null
    textInput.onChangeCustomType {
      argument = it
    }

    diffProducingWidget.sendEvent(Event(Id(1), EventTag(4), JsonPrimitive("PT10S")))

    assertEquals(10.seconds, argument)
  }

  @Test fun unknownEventThrowsDefault() {
    val factory = DiffProducingExampleSchemaWidgetFactory(ProtocolBridge())
    val button = factory.Button() as DiffProducingWidget

    val event = Event(Id(1), EventTag(3456543))
    val t = assertFailsWith<IllegalArgumentException> {
      button.sendEvent(event)
    }

    assertEquals("Unknown event tag 3456543 for widget tag 4", t.message)
  }

  @Test fun unknownEventCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffProducingExampleSchemaWidgetFactory(ProtocolBridge(), mismatchHandler = handler)
    val button = factory.Button() as DiffProducingWidget

    button.sendEvent(Event(Id(1), EventTag(3456543)))

    assertEquals("Unknown event 3456543 for 4", handler.events.single())
  }
}
