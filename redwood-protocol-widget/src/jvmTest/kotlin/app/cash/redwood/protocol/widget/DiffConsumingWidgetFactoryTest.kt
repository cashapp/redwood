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

import app.cash.redwood.protocol.Event
import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import example.redwood.widget.Button
import example.redwood.widget.DiffConsumingExampleSchemaWidgetFactory
import example.redwood.widget.ExampleSchemaWidgetFactory
import example.redwood.widget.Row
import example.redwood.widget.ScopedRow
import example.redwood.widget.Text
import example.redwood.widget.TextInput
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DiffConsumingWidgetFactoryTest {
  @Test fun unknownWidgetThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())

    val t = assertFailsWith<IllegalArgumentException> {
      factory.create(345432)
    }
    assertEquals("Unknown widget kind 345432", t.message)
  }

  @Test fun unknownWidgetCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = EmptyExampleSchemaWidgetFactory(),
      mismatchHandler = handler,
    )

    assertNull(factory.create(345432))

    assertEquals("Unknown widget 345432", handler.events.single())
  }

  @Test fun unknownChildrenThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())
    val button = factory.create(4)!!

    val t = assertFailsWith<IllegalArgumentException> {
      button.children(345432)
    }
    assertEquals("Unknown children tag 345432 for widget kind 4", t.message)
  }

  @Test fun unknownChildrenCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = EmptyExampleSchemaWidgetFactory(),
      mismatchHandler = handler,
    )

    val button = factory.create(4)!!
    assertNull(button.children(345432))

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
    val textInput = factory.create(5)!!

    val throwingEventSink = EventSink { error(it) }
    textInput.apply(PropertyDiff(1L, 2, JsonPrimitive("PT10S")), throwingEventSink)

    assertEquals(recordingTextInput.customType, 10.seconds)
  }

  @Test fun unknownPropertyThrowsDefaults() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(EmptyExampleSchemaWidgetFactory())
    val button = factory.create(4) as DiffConsumingWidget<*>

    val diff = PropertyDiff(1L, 345432)
    val eventSink = EventSink { throw UnsupportedOperationException() }
    val t = assertFailsWith<IllegalArgumentException> {
      button.apply(diff, eventSink)
    }
    assertEquals("Unknown property tag 345432 for widget kind 4", t.message)
  }

  @Test fun unknownPropertyCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = EmptyExampleSchemaWidgetFactory(),
      mismatchHandler = handler,
    )
    val button = factory.create(4) as DiffConsumingWidget<*>

    button.apply(PropertyDiff(1L, 345432)) { throw UnsupportedOperationException() }

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
    val textInput = factory.create(5)!!

    val eventSink = RecordingEventSink()
    textInput.apply(PropertyDiff(1L, 4, JsonPrimitive(true)), eventSink)

    recordingTextInput.onChangeCustomType!!.invoke(10.seconds)

    assertEquals(Event(1L, 4, JsonPrimitive("PT10S")), eventSink.events.single())
  }

  open class EmptyExampleSchemaWidgetFactory : ExampleSchemaWidgetFactory<Nothing> {
    override fun Row(): Row<Nothing> = TODO()
    override fun ScopedRow(): ScopedRow<Nothing> = TODO()
    override fun Text(): Text<Nothing> = TODO()
    override fun Button() = object : Button<Nothing> {
      override val value get() = TODO()
      override fun text(text: String?) = TODO()
      override fun onClick(onClick: (() -> Unit)?) = TODO()
    }
    override fun TextInput(): TextInput<Nothing> = TODO()
  }

  class RecordingTextInput : TextInput<Nothing> {
    override val value get() = TODO()

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
