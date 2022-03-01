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

import app.cash.redwood.protocol.EventSink
import app.cash.redwood.protocol.PropertyDiff
import example.redwood.test.SchemaExampleSchemaWidgetFactory
import example.redwood.widget.DiffConsumingExampleSchemaWidgetFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DiffConsumingWidgetFactoryTest {
  @Test fun unknownWidgetThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(SchemaExampleSchemaWidgetFactory)

    val t = assertFailsWith<IllegalArgumentException> {
      factory.create(345432)
    }
    assertEquals("Unknown widget kind 345432", t.message)
  }

  @Test fun unknownWidgetCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = SchemaExampleSchemaWidgetFactory,
      mismatchHandler = handler,
    )

    assertNull(factory.create(345432))

    assertEquals(listOf("Unknown widget 345432"), handler.events)
  }

  @Test fun unknownChildrenThrowsDefault() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(SchemaExampleSchemaWidgetFactory)
    val button = factory.create(3) as DiffConsumingWidget<*>

    val t = assertFailsWith<IllegalArgumentException> {
      button.children(345432)
    }
    assertEquals("Unknown children tag 345432 for widget kind 3", t.message)
  }

  @Test fun unknownChildrenCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = SchemaExampleSchemaWidgetFactory,
      mismatchHandler = handler,
    )

    val button = factory.create(3) as DiffConsumingWidget<*>
    assertNull(button.children(345432))

    assertEquals(listOf("Unknown children 345432 for 3"), handler.events)
  }

  @Test fun unknownPropertyThrowsDefaults() {
    val factory = DiffConsumingExampleSchemaWidgetFactory(SchemaExampleSchemaWidgetFactory)
    val button = factory.create(3) as DiffConsumingWidget<*>

    val diff = PropertyDiff(1L, 345432)
    val eventSink = EventSink { throw UnsupportedOperationException() }
    val t = assertFailsWith<IllegalArgumentException> {
      button.apply(diff, eventSink)
    }
    assertEquals("Unknown property tag 345432 for widget kind 3", t.message)
  }

  @Test fun unknownPropertyCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffConsumingExampleSchemaWidgetFactory(
      delegate = SchemaExampleSchemaWidgetFactory,
      mismatchHandler = handler,
    )
    val button = factory.create(3) as DiffConsumingWidget<*>

    button.apply(PropertyDiff(1L, 345432)) { throw UnsupportedOperationException() }

    assertEquals(listOf("Unknown property 345432 for 3"), handler.events)
  }
}
