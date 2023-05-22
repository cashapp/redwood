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
package app.cash.redwood.compose.testing

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import app.cash.redwood.widget.Widget.Provider
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test

class WidgetValueTest {
  @Test
  fun flattenNoHierarchy() {
    val a = SimpleWidgetValue()
    val b = SimpleWidgetValue()
    val c = SimpleWidgetValue()

    assertThat(listOf<WidgetValue>().flatten().toList())
      .isEmpty()
    assertThat(listOf<WidgetValue>(a).flatten().toList())
      .containsExactly(a)
    assertThat(listOf<WidgetValue>(a, b, c).flatten().toList())
      .containsExactly(a, b, c)
  }

  @Test
  fun flattenParentsFirst() {
    val a = SimpleWidgetValue()
    val aa = SimpleWidgetValue(childrenLists = listOf(listOf(a)))
    val aaa = SimpleWidgetValue(childrenLists = listOf(listOf(aa)))

    assertThat(listOf<WidgetValue>(aa).flatten().toList())
      .containsExactly(aa, a)
    assertThat(listOf<WidgetValue>(aaa).flatten().toList())
      .containsExactly(aaa, aa, a)
  }

  @Test
  fun flattenSiblingSubtreeFirst() {
    val a = SimpleWidgetValue()
    val aa = SimpleWidgetValue(childrenLists = listOf(listOf(a)))
    val aaa = SimpleWidgetValue(childrenLists = listOf(listOf(aa)))
    val b = SimpleWidgetValue()

    assertThat(listOf<WidgetValue>(aa, b).flatten().toList())
      .containsExactly(aa, a, b)
    assertThat(listOf<WidgetValue>(aaa, b).flatten().toList())
      .containsExactly(aaa, aa, a, b)
  }

  class SimpleWidgetValue(
    override val modifier: Modifier = Modifier,
    override val childrenLists: List<List<WidgetValue>> = listOf(),
  ) : WidgetValue {
    override fun <W : Any> toWidget(provider: Provider<W>): Widget<W> {
      throw AssertionError()
    }
  }
}
