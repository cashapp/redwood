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
import app.cash.redwood.protocol.LayoutModifierElement
import app.cash.redwood.protocol.LayoutModifierTag
import app.cash.redwood.protocol.PropertyDiff
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import example.redwood.compose.TestScope
import example.redwood.widget.ExampleSchemaProtocolNodeFactory
import example.redwood.widget.ExampleSchemaWidgetFactories
import example.redwood.widget.TextInput
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.modules.SerializersModule

class ProtocolNodeFactoryTest {
  @Test fun unknownWidgetThrowsDefault() {
    val factory = ExampleSchemaProtocolNodeFactory(
      ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
    )

    val t = assertFailsWith<IllegalArgumentException> {
      factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(345432))
    }
    assertThat(t).hasMessage("Unknown widget tag 345432")
  }

  @Test fun unknownWidgetCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      mismatchHandler = handler,
    )

    assertThat(factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(345432))).isNull()

    assertThat(handler.events.single()).isEqualTo("Unknown widget 345432")
  }

  @Test fun layoutModifierUsesSerializerModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = object : EmptyExampleSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    textInput.updateLayoutModifier(
      listOf(
        LayoutModifierElement(
          tag = LayoutModifierTag(3),
          value = buildJsonObject {
            put("customType", JsonPrimitive("PT10S"))
          },
        ),
      ),
    )

    with(object : TestScope {}) {
      assertThat(recordingTextInput.layoutModifiers).isEqualTo(LayoutModifier.customType(10.seconds))
    }
  }

  @Test fun layoutModifierDeserializationHonorsDefaultExpressions() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = object : EmptyExampleSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    textInput.updateLayoutModifier(
      listOf(
        LayoutModifierElement(
          tag = LayoutModifierTag(5),
          value = buildJsonObject {
            put("customType", JsonPrimitive("PT10S"))
          },
        ),
      ),
    )

    with(object : TestScope {}) {
      assertThat(recordingTextInput.layoutModifiers).isEqualTo(
        LayoutModifier.customTypeWithDefault(
          10.seconds,
          "sup",
        ),
      )
    }
  }

  @Test fun unknownLayoutModifierThrowsDefault() {
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
    )
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.updateLayoutModifier(
        listOf(
          LayoutModifierElement(
            tag = LayoutModifierTag(345432),
            value = JsonObject(mapOf()),
          ),
        ),
      )
    }
    assertThat(t).hasMessage("Unknown layout modifier tag 345432")
  }

  @Test fun unknownLayoutModifierCallsHandler() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val handler = RecordingProtocolMismatchHandler()
    val recordingTextInput = RecordingTextInput()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = object : EmptyExampleSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      json = json,
      mismatchHandler = handler,
    )

    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!
    textInput.updateLayoutModifier(
      listOf(
        LayoutModifierElement(
          tag = LayoutModifierTag(345432),
          value = buildJsonArray {
            add(JsonPrimitive(345432))
            add(JsonObject(mapOf()))
          },
        ),
        LayoutModifierElement(
          tag = LayoutModifierTag(2),
          value = buildJsonObject { put("value", JsonPrimitive("hi")) },
        ),
      ),
    )

    assertThat(handler.events.single()).isEqualTo("Unknown layout modifier 345432")

    // Ensure only the invalid LayoutModifier was discarded and not all of them.
    with(object : TestScope {}) {
      assertThat(recordingTextInput.layoutModifiers).isEqualTo(
        LayoutModifier.accessibilityDescription(
          "hi",
        ),
      )
    }
  }

  @Test fun unknownChildrenThrowsDefault() {
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
    )
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.children(ChildrenTag(345432))
    }
    assertThat(t).hasMessage("Unknown children tag 345432 for widget tag 4")
  }

  @Test fun unknownChildrenCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      mismatchHandler = handler,
    )

    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!
    assertThat(button.children(ChildrenTag(345432))).isNull()

    assertThat(handler.events.single()).isEqualTo("Unknown children 345432 for 4")
  }

  @Test fun propertyUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = object : EmptyExampleSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    val throwingEventSink = EventSink { error(it) }
    textInput.apply(PropertyDiff(Id(1), PropertyTag(2), JsonPrimitive("PT10S")), throwingEventSink)

    assertThat(recordingTextInput.customType).isEqualTo(10.seconds)
  }

  @Test fun unknownPropertyThrowsDefaults() {
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
    )
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    val diff = PropertyDiff(Id(1), PropertyTag(345432))
    val eventSink = EventSink { throw UnsupportedOperationException() }
    val t = assertFailsWith<IllegalArgumentException> {
      button.apply(diff, eventSink)
    }
    assertThat(t).hasMessage("Unknown property tag 345432 for widget tag 4")
  }

  @Test fun unknownPropertyCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = EmptyExampleSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      mismatchHandler = handler,
    )
    val button = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(4))!!

    button.apply(PropertyDiff(Id(1), PropertyTag(345432))) { throw UnsupportedOperationException() }

    assertThat(handler.events.single()).isEqualTo("Unknown property 345432 for 4")
  }

  @Test fun eventUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = ExampleSchemaProtocolNodeFactory(
      provider = ExampleSchemaWidgetFactories(
        ExampleSchema = object : EmptyExampleSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(Id.Root, ThrowingWidgetChildren(), WidgetTag(5))!!

    val eventSink = RecordingEventSink()
    textInput.apply(PropertyDiff(Id(1), PropertyTag(4), JsonPrimitive(true)), eventSink)

    recordingTextInput.onChangeCustomType!!.invoke(10.seconds)

    assertThat(eventSink.events.single())
      .isEqualTo(Event(Id(1), EventTag(4), JsonPrimitive("PT10S")))
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

    override fun onChange(onChange: (String) -> Unit) {
      this.onChange = onChange
    }

    var onChangeCustomType: ((Duration) -> Unit)? = null
      private set

    override fun onChangeCustomType(onChangeCustomType: (Duration) -> Unit) {
      this.onChangeCustomType = onChangeCustomType
    }

    var maxLength: Int? = null
      private set

    override fun maxLength(maxLength: Int) {
      this.maxLength = maxLength
    }
  }
}
