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

import app.cash.redwood.Modifier
import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.ModifierElement
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyChange
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.example.redwood.testing.compose.TestScope
import com.example.redwood.testing.widget.TestSchemaProtocolNodeFactory
import com.example.redwood.testing.widget.TestSchemaWidgetFactories
import com.example.redwood.testing.widget.TextInput
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
    val factory = TestSchemaProtocolNodeFactory(
      TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
    )

    val t = assertFailsWith<IllegalArgumentException> {
      factory.create(WidgetTag(345432))
    }
    assertThat(t).hasMessage("Unknown widget tag 345432")
  }

  @Test fun unknownWidgetCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      mismatchHandler = handler,
    )

    assertThat(factory.create(WidgetTag(345432))).isNull()

    assertThat(handler.events.single()).isEqualTo("Unknown widget 345432")
  }

  @Test fun modifierUsesSerializerModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = object : EmptyTestSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(WidgetTag(5))!!

    textInput.updateModifier(
      listOf(
        ModifierElement(
          tag = ModifierTag(3),
          value = buildJsonObject {
            put("customType", JsonPrimitive("PT10S"))
          },
        ),
      ),
    )

    with(object : TestScope {}) {
      assertThat(recordingTextInput.modifier).isEqualTo(Modifier.customType(10.seconds))
    }
  }

  @Test fun modifierDeserializationHonorsDefaultExpressions() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = object : EmptyTestSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(WidgetTag(5))!!

    textInput.updateModifier(
      listOf(
        ModifierElement(
          tag = ModifierTag(5),
          value = buildJsonObject {
            put("customType", JsonPrimitive("PT10S"))
          },
        ),
      ),
    )

    with(object : TestScope {}) {
      assertThat(recordingTextInput.modifier).isEqualTo(
        Modifier.customTypeWithDefault(
          10.seconds,
          "sup",
        ),
      )
    }
  }

  @Test fun unknownModifierThrowsDefault() {
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
    )
    val button = factory.create(WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.updateModifier(
        listOf(
          ModifierElement(
            tag = ModifierTag(345432),
            value = JsonObject(mapOf()),
          ),
        ),
      )
    }
    assertThat(t).hasMessage("Unknown layout modifier tag 345432")
  }

  @Test fun unknownModifierCallsHandler() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val handler = RecordingProtocolMismatchHandler()
    val recordingTextInput = RecordingTextInput()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = object : EmptyTestSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      json = json,
      mismatchHandler = handler,
    )

    val textInput = factory.create(WidgetTag(5))!!
    textInput.updateModifier(
      listOf(
        ModifierElement(
          tag = ModifierTag(345432),
          value = buildJsonArray {
            add(JsonPrimitive(345432))
            add(JsonObject(mapOf()))
          },
        ),
        ModifierElement(
          tag = ModifierTag(2),
          value = buildJsonObject { put("value", JsonPrimitive("hi")) },
        ),
      ),
    )

    assertThat(handler.events.single()).isEqualTo("Unknown layout modifier 345432")

    // Ensure only the invalid Modifier was discarded and not all of them.
    with(object : TestScope {}) {
      assertThat(recordingTextInput.modifier).isEqualTo(
        Modifier.accessibilityDescription(
          "hi",
        ),
      )
    }
  }

  @Test fun unknownChildrenThrowsDefault() {
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
    )
    val button = factory.create(WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.children(ChildrenTag(345432))
    }
    assertThat(t).hasMessage("Unknown children tag 345432 for widget tag 4")
  }

  @Test fun unknownChildrenCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      mismatchHandler = handler,
    )

    val button = factory.create(WidgetTag(4))!!
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
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = object : EmptyTestSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(WidgetTag(5))!!

    val throwingEventSink = EventSink { error(it) }
    textInput.apply(PropertyChange(Id(1), PropertyTag(2), JsonPrimitive("PT10S")), throwingEventSink)

    assertThat(recordingTextInput.customType).isEqualTo(10.seconds)
  }

  @Test fun unknownPropertyThrowsDefaults() {
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
    )
    val button = factory.create(WidgetTag(4))!!

    val change = PropertyChange(Id(1), PropertyTag(345432))
    val eventSink = EventSink { throw UnsupportedOperationException() }
    val t = assertFailsWith<IllegalArgumentException> {
      button.apply(change, eventSink)
    }
    assertThat(t).hasMessage("Unknown property tag 345432 for widget tag 4")
  }

  @Test fun unknownPropertyCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = EmptyTestSchemaWidgetFactory(),
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      mismatchHandler = handler,
    )
    val button = factory.create(WidgetTag(4))!!

    button.apply(PropertyChange(Id(1), PropertyTag(345432))) { throw UnsupportedOperationException() }

    assertThat(handler.events.single()).isEqualTo("Unknown property 345432 for 4")
  }

  @Test fun eventUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val recordingTextInput = RecordingTextInput()
    val factory = TestSchemaProtocolNodeFactory(
      provider = TestSchemaWidgetFactories(
        TestSchema = object : EmptyTestSchemaWidgetFactory() {
          override fun TextInput() = recordingTextInput
        },
        RedwoodLayout = EmptyRedwoodLayoutWidgetFactory(),
        RedwoodLazyLayout = EmptyRedwoodLazyLayoutWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.create(WidgetTag(5))!!

    val eventSink = RecordingEventSink()
    textInput.apply(PropertyChange(Id(1), PropertyTag(4), JsonPrimitive(true)), eventSink)

    recordingTextInput.onChangeCustomType!!.invoke(10.seconds)

    assertThat(eventSink.events.single())
      .isEqualTo(Event(Id(1), EventTag(4), listOf(JsonPrimitive("PT10S"))))
  }

  class RecordingTextInput : TextInput<Nothing> {
    override val value get() = TODO()
    override var modifier: Modifier = Modifier

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
