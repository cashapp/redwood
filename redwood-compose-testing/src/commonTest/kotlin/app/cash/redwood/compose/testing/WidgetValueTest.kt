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

import app.cash.redwood.LayoutModifier
import kotlin.test.Test
import kotlin.test.assertEquals

class WidgetValueTest {
  @Test
  fun flattenNoHierarchy() {
    val a = SimpleWidgetValue()
    val b = SimpleWidgetValue()
    val c = SimpleWidgetValue()

    assertEquals(
      listOf(),
      listOf<WidgetValue>().flatten().toList(),
    )
    assertEquals(
      listOf(a),
      listOf<WidgetValue>(a).flatten().toList(),
    )
    assertEquals(
      listOf(a, b, c),
      listOf<WidgetValue>(a, b, c).flatten().toList(),
    )
  }

  @Test
  fun flattenParentsFirst() {
    val a = SimpleWidgetValue()
    val aa = SimpleWidgetValue(childrenLists = listOf(listOf(a)))
    val aaa = SimpleWidgetValue(childrenLists = listOf(listOf(aa)))

    assertEquals(
      listOf(aa, a),
      listOf<WidgetValue>(aa).flatten().toList(),
    )
    assertEquals(
      listOf(aaa, aa, a),
      listOf<WidgetValue>(aaa).flatten().toList(),
    )
  }

  @Test
  fun flattenSiblingSubtreeFirst() {
    val a = SimpleWidgetValue()
    val aa = SimpleWidgetValue(childrenLists = listOf(listOf(a)))
    val aaa = SimpleWidgetValue(childrenLists = listOf(listOf(aa)))
    val b = SimpleWidgetValue()

    assertEquals(
      listOf(aa, a, b),
      listOf<WidgetValue>(aa, b).flatten().toList(),
    )
    assertEquals(
      listOf(aaa, aa, a, b),
      listOf<WidgetValue>(aaa, b).flatten().toList(),
    )
  }

  class SimpleWidgetValue(
    override val layoutModifiers: LayoutModifier = LayoutModifier,
    override val childrenLists: List<List<WidgetValue>> = listOf()
  ) : WidgetValue
}
