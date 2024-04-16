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
package app.cash.redwood.protocol.host

import app.cash.redwood.Modifier
import app.cash.redwood.RedwoodCodegenApi
import app.cash.redwood.layout.testing.RedwoodLayoutTestingWidgetFactory
import app.cash.redwood.lazylayout.testing.RedwoodLazyLayoutTestingWidgetFactory
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
import com.example.redwood.testing.protocol.host.TestSchemaProtocolFactory
import com.example.redwood.testing.testing.TestSchemaTestingWidgetFactory
import com.example.redwood.testing.testing.TextInputValue
import com.example.redwood.testing.widget.TestSchemaWidgetSystem
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

@OptIn(RedwoodCodegenApi::class)
class ProtocolFactoryTest {
  @Test fun unknownWidgetThrowsDefault() {
    val factory = TestSchemaProtocolFactory(
      TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
    )

    val t = assertFailsWith<IllegalArgumentException> {
      factory.createNode(Id(1), WidgetTag(345432))
    }
    assertThat(t).hasMessage("Unknown widget tag 345432")
  }

  @Test fun unknownWidgetCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      mismatchHandler = handler,
    )

    assertThat(factory.createNode(Id(1), WidgetTag(345432))).isNull()

    assertThat(handler.events.single()).isEqualTo("Unknown widget 345432")
  }

  @Test fun modifierUsesSerializerModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      json = json,
    )

    val modifier = factory.createModifier(
      ModifierElement(
        tag = ModifierTag(3),
        value = buildJsonObject {
          put("customType", JsonPrimitive("PT10S"))
        },
      ),
    )

    with(object : TestScope {}) {
      assertThat(modifier).isEqualTo(Modifier.customType(10.seconds))
    }
  }

  @Test fun modifierDeserializationHonorsDefaultExpressions() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      json = json,
    )

    val modifier = factory.createModifier(
      ModifierElement(
        tag = ModifierTag(5),
        value = buildJsonObject {
          put("customType", JsonPrimitive("PT10S"))
        },
      ),
    )

    with(object : TestScope {}) {
      assertThat(modifier).isEqualTo(
        Modifier.customTypeWithDefault(
          10.seconds,
          "sup",
        ),
      )
    }
  }

  @Test fun unknownModifierThrowsDefault() {
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
    )

    val t = assertFailsWith<IllegalArgumentException> {
      factory.createModifier(
        ModifierElement(
          tag = ModifierTag(345432),
          value = JsonObject(mapOf()),
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
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      json = json,
      mismatchHandler = handler,
    )

    val modifier = factory.createModifier(
      ModifierElement(
        tag = ModifierTag(345432),
        value = buildJsonArray {
          add(JsonPrimitive(345432))
          add(JsonObject(mapOf()))
        },
      ),
    ) then factory.createModifier(
      ModifierElement(
        tag = ModifierTag(2),
        value = buildJsonObject { put("value", JsonPrimitive("hi")) },
      ),
    )

    assertThat(handler.events.single()).isEqualTo("Unknown layout modifier 345432")

    // Ensure only the invalid Modifier was discarded and not all of them.
    with(object : TestScope {}) {
      assertThat(modifier).isEqualTo(
        Modifier.accessibilityDescription(
          "hi",
        ),
      )
    }
  }

  @Test fun unknownChildrenThrowsDefault() {
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
    )
    val button = factory.createNode(Id(1), WidgetTag(4))!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.children(ChildrenTag(345432))
    }
    assertThat(t).hasMessage("Unknown children tag 345432 for widget tag 4")
  }

  @Test fun unknownChildrenCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      mismatchHandler = handler,
    )

    val button = factory.createNode(Id(1), WidgetTag(4))!!
    assertThat(button.children(ChildrenTag(345432))).isNull()

    assertThat(handler.events.single()).isEqualTo("Unknown children 345432 for 4")
  }

  @Test fun propertyUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.createNode(Id(1), WidgetTag(5))!!

    val throwingEventSink = EventSink { error(it) }
    textInput.apply(PropertyChange(Id(1), PropertyTag(2), JsonPrimitive("PT10S")), throwingEventSink)

    assertThat((textInput.widget.value as TextInputValue).customType).isEqualTo(10.seconds)
  }

  @Test fun unknownPropertyThrowsDefaults() {
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
    )
    val button = factory.createNode(Id(1), WidgetTag(4))!!

    val change = PropertyChange(Id(1), PropertyTag(345432))
    val eventSink = EventSink { throw UnsupportedOperationException() }
    val t = assertFailsWith<IllegalArgumentException> {
      button.apply(change, eventSink)
    }
    assertThat(t).hasMessage("Unknown property tag 345432 for widget tag 4")
  }

  @Test fun unknownPropertyCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      mismatchHandler = handler,
    )
    val button = factory.createNode(Id(1), WidgetTag(4))!!

    button.apply(PropertyChange(Id(1), PropertyTag(345432))) { throw UnsupportedOperationException() }

    assertThat(handler.events.single()).isEqualTo("Unknown property 345432 for 4")
  }

  @Test fun eventUsesSerializersModule() {
    val json = Json {
      serializersModule = SerializersModule {
        contextual(Duration::class, DurationIsoSerializer)
      }
    }
    val factory = TestSchemaProtocolFactory(
      widgetSystem = TestSchemaWidgetSystem(
        TestSchema = TestSchemaTestingWidgetFactory(),
        RedwoodLayout = RedwoodLayoutTestingWidgetFactory(),
        RedwoodLazyLayout = RedwoodLazyLayoutTestingWidgetFactory(),
      ),
      json = json,
    )
    val textInput = factory.createNode(Id(1), WidgetTag(5))!!

    val eventSink = RecordingEventSink()
    textInput.apply(PropertyChange(Id(1), PropertyTag(4), JsonPrimitive(true)), eventSink)

    (textInput.widget.value as TextInputValue).onChangeCustomType!!.invoke(10.seconds)

    assertThat(eventSink.events.single())
      .isEqualTo(Event(Id(1), EventTag(4), listOf(JsonPrimitive("PT10S"))))
  }
}
