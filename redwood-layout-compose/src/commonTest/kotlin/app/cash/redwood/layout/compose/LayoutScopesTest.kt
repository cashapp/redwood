/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.layout.compose

import app.cash.redwood.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutScopesTest {
  @Test
  fun flexIsGrowAndShrink() {
    val flex = Modifier.flex(0.5)
    val components = flex.components()
    assertEquals(
      listOf(GrowImpl(0.5), ShrinkImpl(0.5)),
      components,
    )
  }

  private fun Modifier.components(): List<Modifier> {
    return buildList {
      this@components.forEach {
        add(it)
      }
    }
  }
}
