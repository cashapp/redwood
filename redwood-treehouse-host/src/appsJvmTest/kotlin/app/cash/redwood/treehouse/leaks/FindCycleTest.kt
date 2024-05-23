/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.treehouse.leaks

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test

internal class FindCycleTest {
  @Test
  fun cycle() {
    val heap = object : Heap {
      override fun references(instance: Any) = when (instance) {
        "A" -> listOf(
          Edge("b", "B"),
          Edge("c", "C"),
        )

        "B" -> listOf(
          Edge("d", "D"),
        )

        "C" -> listOf(
          Edge("d", "D"),
        )

        "D" -> listOf(
          Edge("a", "A"),
        )

        else -> listOf()
      }
    }

    assertThat(heap.findCycle("A"))
      .isNotNull()
      .containsExactly("b", "d", "a")
    assertThat(heap.findCycle("B"))
      .isNotNull()
      .containsExactly("d", "a", "b")
    assertThat(heap.findCycle("C"))
      .isNotNull()
      .containsExactly("d", "a", "c")
    assertThat(heap.findCycle("D"))
      .isNotNull()
      .containsExactly("a", "b", "d")
  }

  @Test
  fun happyPathWithNoCycle() {
    val heap = object : Heap {
      override fun references(instance: Any) = when (instance) {
        "A" -> listOf(
          Edge("b", "B"),
          Edge("c", "C"),
        )

        "B" -> listOf(
          Edge("d", "D"),
        )

        "C" -> listOf(
          Edge("d", "D"),
        )

        else -> listOf()
      }
    }

    assertThat(heap.findCycle("A")).isNull()
    assertThat(heap.findCycle("B")).isNull()
    assertThat(heap.findCycle("C")).isNull()
  }

  @Test
  fun directCycle() {
    val heap = object : Heap {
      override fun references(instance: Any) = when (instance) {
        "A" -> listOf(
          Edge("a", "A"),
        )

        else -> listOf()
      }
    }

    assertThat(heap.findCycle("A"))
      .isNotNull()
      .containsExactly("a")
  }
}
