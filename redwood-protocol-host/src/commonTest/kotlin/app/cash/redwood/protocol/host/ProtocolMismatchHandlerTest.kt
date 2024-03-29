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

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.ModifierTag
import app.cash.redwood.protocol.PropertyTag
import app.cash.redwood.protocol.WidgetTag
import assertk.assertThat
import assertk.assertions.hasMessage
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ProtocolMismatchHandlerTest {
  @Test fun throwingUnknownWidget() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownWidget(WidgetTag(1))
    }
    assertThat(t).hasMessage("Unknown widget tag 1")
  }

  @Test fun throwingUnknownModifier() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownModifier(ModifierTag(1))
    }
    assertThat(t).hasMessage("Unknown layout modifier tag 1")
  }

  @Test fun throwingUnknownChildren() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownChildren(WidgetTag(1), ChildrenTag(2))
    }
    assertThat(t).hasMessage("Unknown children tag 2 for widget tag 1")
  }

  @Test fun throwingUnknownProperty() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownProperty(WidgetTag(1), PropertyTag(2))
    }
    assertThat(t).hasMessage("Unknown property tag 2 for widget tag 1")
  }
}
