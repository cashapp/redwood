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

import app.cash.redwood.protocol.EventTag
import app.cash.redwood.protocol.Id
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.hasMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProtocolMismatchHandlerTest {
  @Test fun throwingUnknownEvent() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownEvent(WidgetTag(1), EventTag(2))
    }
    assertThat(t).hasMessage("Unknown event tag 2 for widget tag 1")
  }

  @Test fun throwingUnknownEventNode() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownEventNode(Id(1), EventTag(2))
    }
    assertThat(t).hasMessage("Unknown node ID 1 for event with tag 2")
  }
}
