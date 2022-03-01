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

import app.cash.redwood.protocol.Event
import example.redwood.compose.DiffProducingExampleSchemaWidgetFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DiffProducingWidgetFactoryTest {
  @Test fun unknownEventThrowsDefault() {
    val factory = DiffProducingExampleSchemaWidgetFactory()
    val button = factory.Button() as AbstractDiffProducingWidget

    val event = Event(1L, 3456543)
    val t = assertFailsWith<IllegalArgumentException> {
      button.sendEvent(event)
    }

    assertEquals("Unknown event tag 3456543 for widget kind 3", t.message)
  }

  @Test fun unknownEventCallsHandler() {
    val handler = RecordingProtocolMismatchHandler()
    val factory = DiffProducingExampleSchemaWidgetFactory(mismatchHandler = handler)
    val button = factory.Button() as AbstractDiffProducingWidget

    button.sendEvent(Event(1L, 3456543))

    assertEquals(listOf("Unknown event 3456543 for 3"), handler.events)
  }
}
