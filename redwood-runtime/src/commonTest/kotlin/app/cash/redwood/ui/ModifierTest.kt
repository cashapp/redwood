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
package app.cash.redwood.ui

import app.cash.redwood.Modifier
import assertk.assertThat
import assertk.assertions.hashCodeFun
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import assertk.assertions.toStringFun
import kotlin.test.Test
import kotlin.test.fail

class ModifierTest {
  @Test fun forEachUnitModifierNeverInvoked() {
    Modifier.forEach {
      fail()
    }
  }

  @Test fun forEachOneElement() {
    val a = NamedModifier("A")
    var called = 0
    a.forEach { element ->
      assertThat(element).isSameInstanceAs(a)
      called++
    }
    assertThat(called).isEqualTo(1)
  }

  @Test fun forEachManyElements() {
    val a = NamedModifier("A")
    val b = NamedModifier("B")
    val c = NamedModifier("C")
    val expected = listOf(a, b, c)
    var called = 0
    (a then b then c).forEach { element ->
      assertThat(element).isSameInstanceAs(expected[called])
      called++
    }
    assertThat(called).isEqualTo(3)
  }

  @Test fun thenIgnoresUnitModifier() {
    assertThat(Modifier then Modifier).isSameInstanceAs(Modifier)

    val a = NamedModifier("A")
    assertThat(a then Modifier).isSameInstanceAs(a)
    assertThat(Modifier then a).isSameInstanceAs(a)
  }

  @Test fun thenElementsToElements() {
    val a = NamedModifier("A")
    val b = NamedModifier("B")
    val c = NamedModifier("C")
    val d = NamedModifier("D")
    val expected = listOf(a, b, c, d)
    var called = 0
    ((a then b) then (c then d)).forEach { element ->
      assertThat(element).isSameInstanceAs(expected[called])
      called++
    }
    assertThat(called).isEqualTo(4)
  }

  @Test fun toStringEmpty() {
    assertThat(Modifier).toStringFun().isEqualTo("Modifier")
  }

  @Test fun toStringOne() {
    val a = NamedModifier("A")
    assertThat(a).toStringFun().isEqualTo("A")
  }

  @Test fun toStringMany() {
    val a = NamedModifier("A")
    val b = NamedModifier("B")
    val c = NamedModifier("C")
    assertThat((a then b) then c).toStringFun().isEqualTo("[A, B, C]")
    assertThat(a then (b then c)).toStringFun().isEqualTo("[A, B, C]")
  }

  @Test fun hashCodeMany() {
    val a = NamedModifier("A")
    val b = NamedModifier("B")
    val c = NamedModifier("C")
    assertThat((a then b) then c).hashCodeFun().isEqualTo((a then (b then c)).hashCode())
  }

  @Test fun equalsMany() {
    val a = NamedModifier("A")
    val b = NamedModifier("B")
    val c = NamedModifier("C")
    assertThat((a then b) then c).isEqualTo(a then (b then c))
    assertThat(a then (b then c)).isEqualTo((a then b) then c)
  }
}

private class NamedModifier(private val name: String) : Modifier.Element {
  override fun toString() = name
}
