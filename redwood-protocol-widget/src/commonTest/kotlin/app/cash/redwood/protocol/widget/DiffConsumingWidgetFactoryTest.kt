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
package app.cash.redwood.protocol.widget

import app.cash.redwood.LayoutModifier
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import example.redwood.compose.TestScope
import example.redwood.widget.DiffConsumingExampleSchemaWidgetFactory
import example.redwood.widget.TextInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.modules.SerializersModule

class DiffConsumingWidgetFactoryTest {
  @Test fun unknownWidgetThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())

    val t = assertFailsWith<IllegalArgumentException> {
      factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(345432))
    }
    assertEquals("Unknown widget tag 345432", t.message)
  }

  @Test fun unknownWidgetCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = EmptyExampleSchemaWidgetFactory(),
      mismatchHandler = handler,
    )

    assertNull(factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(345432)))

    assertEquals("Unknown widget 345432", handler.events.single())
  }

  @Test fun layoutModifierUsesSerializerModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = object : EmptyExampleSchemaWidgetFactory() {
        override fun TextInput() = recordingTextInput
      },
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    textInput.updateLayoutModifier(
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
    )

    with(object : TestScope {}) {
      assertEquals(LayoutModifier.customType(10.seconds), recordingTextInput.layoutModifiers)
    }
  }

  @Test fun layoutModifierDeserializationHonorsDefaultExpressions() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = object : EmptyExampleSchemaWidgetFactory() {
        override fun TextInput() = recordingTextInput
      },
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    textInput.updateLayoutModifier(
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
    )

    with(object : TestScope {}) {
      assertEquals(LayoutModifier.customTypeWithDefault(10.seconds, "sup"), recordingTextInput.layoutModifiers)
    }
  }

  @Test fun unknownLayoutModifierThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.updateLayoutModifier(
        JsonArray(
          listOf(
            buildJsonArray {
              add(JsonPrimitive(345432))
              add(JsonObject(mapOf()))
            },
          ),
        ),
      )
    }
    assertEquals("Unknown layout modifier tag 345432", t.message)
  }

  @Test fun unknownLayoutModifierCallsHandler() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val handler = RecordingProtocolMismatchHandler()
    val recordingTextInput = RecordingTextInput()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = object : EmptyExampleSchemaWidgetFactory() {
        override fun TextInput() = recordingTextInput
      },
      json = json,
      mismatchHandler = handler,
    )

    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!
    textInput.updateLayoutModifier(
      buildJsonArray {
        add(
          buildJsonArray {
            add(JsonPrimitive(345432))
            add(JsonObject(mapOf()))
          },
        )
        add(
          buildJsonArray {
            add(JsonPrimitive(2))
            add(buildJsonObject { put("value", JsonPrimitive("hi")) })
          },
        )
      },
    )

    assertEquals("Unknown layout modifier 345432", handler.events.single())

    // Ensure only the invalid LayoutModifier was discarded and not all of them.
    with(object : TestScope {}) {
      assertEquals(LayoutModifier.accessibilityDescription("hi"), recordingTextInput.layoutModifiers)
    }
  }

  @Test fun unknownChildrenThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.children(ChildrenTag(345432))
    }
    assertEquals("Unknown children tag 345432 for widget tag 4", t.message)
  }

  @Test fun unknownChildrenCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = EmptyExampleSchemaWidgetFactory(),
      mismatchHandler = handler,
    )

    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!
    assertNull(button.children(ChildrenTag(345432)))

    assertEquals("Unknown children 345432 for 4", handler.events.single())
  }

  @Test fun propertyUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = object : EmptyExampleSchemaWidgetFactory() {
        override fun TextInput() = recordingTextInput
      },
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    val throwingEventSink = EventSink { error(it) }
    textInput.apply(PropertyDiff(Id(1), PropertyTag(2), JsonPrimitive("PT10S")), throwingEventSink)

    assertEquals(10.seconds, recordingTextInput.customType)
  }

  @Test fun unknownPropertyThrowsDefaults() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    val diff = PropertyDiff(Id(1), PropertyTag(345432))
    val eventSink = EventSink { throw UnsupportedOperationException() }
    val t = assertFailsWith<IllegalArgumentException> {
      button.apply(diff, eventSink)
    }
    assertEquals("Unknown property tag 345432 for widget tag 4", t.message)
  }

  @Test fun unknownPropertyCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = EmptyExampleSchemaWidgetFactory(),
      mismatchHandler = handler,
    )
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    button.apply(PropertyDiff(Id(1), PropertyTag(345432))) { throw UnsupportedOperationException() }

    assertEquals("Unknown property 345432 for 4", handler.events.single())
  }

  @Test fun eventUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = object : EmptyExampleSchemaWidgetFactory() {
        override fun TextInput() = recordingTextInput
      },
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    val eventSink = RecordingEventSink()
    textInput.apply(PropertyDiff(Id(1), PropertyTag(4), JsonPrimitive(true)), eventSink)

    recordingTextInput.onChangeCustomType!!.invoke(10.seconds)

    assertEquals(Event(Id(1), EventTag(4), JsonPrimitive("PT10S")), eventSink.events.single())
  }

  class RecordingTextInput : TextInput<Nothing> {
    override val value get() = TODO()
    override var layoutModifiers: LayoutModifier = LayoutModifier

    var text: String? = null
      private set

    override fun text(text: String?) {
      this.text = text
    }

    var customType: Duration? = null
      private set

    override fun customType(customType: Duration?) {
      this.customType = customType
    }

    var onChange: ((String) -> Unit)? = null
      private set

    override fun onChange(onChange: ((String) -> Unit)?) {
      this.onChange = onChange
    }

    var onChangeCustomType: ((Duration) -> Unit)? = null
      private set

    override fun onChangeCustomType(onChangeCustomType: ((Duration) -> Unit)?) {
      this.onChangeCustomType = onChangeCustomType
    }
  }
}
