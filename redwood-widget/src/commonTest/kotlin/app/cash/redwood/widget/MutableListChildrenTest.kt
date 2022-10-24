/*
 * Copyright (C) 2021 Square, Inc.
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
package app.cash.redwood.widget

import kotlin.test.Test
import kotlin.test.assertEquals

class MutableListChildrenTest : AbstractWidgetChildrenTest<String>() {
  private var updateCount = Int.MIN_VALUE
  override val children = MutableListChildren<String> { updateCount++ }
  override fun widget(name: String) = name
  override fun names() = children.map { it.value }

  @Test fun insertCallsUpdate() {
    updateCount = 0
    children.insert(0, "one")
    assertEquals(1, updateCount)
  }

  @Test fun moveCallsUpdate() {
    children.insert(0, "one")
    children.insert(1, "two")

    updateCount = 0
    children.move(0, 1, 1)
    assertEquals(1, updateCount)
  }

  @Test fun removeCallsUpdate() {
    children.insert(0, "one")
    children.insert(1, "two")

    updateCount = 0
    children.remove(0, 1)
    assertEquals(1, updateCount)
  }

  @Test fun clearCallsUpdate() {
    children.insert(0, "one")
    children.insert(1, "two")

    updateCount = 0
    children.clear()
    assertEquals(1, updateCount)
  }
}
