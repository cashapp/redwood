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

import app.cash.redwood.protocol.ChildrenTag
import app.cash.redwood.protocol.PropertyTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ProtocolMismatchHandlerTest {
  @Test fun throwingUnknownWidget() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownWidget(1)
    }
    assertEquals("Unknown widget kind 1", t.message)
  }

  @Test fun throwingUnknownLayoutModifier() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownLayoutModifier(1)
    }
    assertEquals("Unknown layout modifier tag 1", t.message)
  }

  @Test fun throwingUnknownChildren() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownChildren(1, ChildrenTag(2))
    }
    assertEquals("Unknown children tag 2 for widget kind 1", t.message)
  }

  @Test fun throwingUnknownProperty() {
    val t = assertFailsWith<IllegalArgumentException> {
      ProtocolMismatchHandler.Throwing.onUnknownProperty(1, PropertyTag(2))
    }
    assertEquals("Unknown property tag 2 for widget kind 1", t.message)
  }
}
