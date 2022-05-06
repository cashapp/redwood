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
  override val children = MutableListChildren<String>()
  override fun names() = children.list
  override fun widget(name: String) = object : Widget<String> {
    override val value get() = name
  }

  @Test fun iterableIteratesChildren() {
    assertEquals(emptyList(), children.toList())

    children.insert(0, widget("one"))
    assertEquals(listOf("one"), children.toList())
  }
}
