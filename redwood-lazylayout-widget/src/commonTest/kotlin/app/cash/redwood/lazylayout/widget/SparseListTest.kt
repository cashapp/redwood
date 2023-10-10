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
package app.cash.redwood.lazylayout.widget

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SparseListTest {
  @Test
  fun nonNulls() {
    val list = SparseList<String?>()
    list.add("A")
    list.add("B")
    list.add("C")
    assertThat(list[0]).isEqualTo("A")
    assertThat(list[1]).isEqualTo("B")
    assertThat(list[2]).isEqualTo("C")
    assertThat(list.size).isEqualTo(3)
    assertThat(list.toList()).containsExactly("A", "B", "C")
  }

  @Test
  fun emptyList() {
    val list = SparseList<String?>()
    assertThat(list.size).isEqualTo(0)
    assertThat(list.toList()).containsExactly()
  }

  @Test
  fun singleNonNull() {
    val list = SparseList<String?>()
    list.add("A")
    assertThat(list[0]).isEqualTo("A")
    assertThat(list.size).isEqualTo(1)
    assertThat(list.toList()).containsExactly("A")
  }

  @Test
  fun singleNull() {
    val list = SparseList<String?>()
    list.add(null)
    assertThat(list[0]).isEqualTo(null)
    assertThat(list.size).isEqualTo(1)
    assertThat(list.toList()).containsExactly(null)
  }

  @Test
  fun allNulls() {
    val list = SparseList<String?>()
    list.add(null)
    list.add(null)
    list.add(null)
    assertThat(list[0]).isEqualTo(null)
    assertThat(list[1]).isEqualTo(null)
    assertThat(list[2]).isEqualTo(null)
    assertThat(list.size).isEqualTo(3)
    assertThat(list.toList()).containsExactly(null, null, null)
  }

  @Test
  fun mixOfNonNullsAndNulls() {
    val list = SparseList<String?>()
    list.add(null)
    list.add(null)
    list.add("A")
    list.add(null)
    list.add("B")
    list.add(null)
    list.add(null)
    list.add(null)
    list.add("C")
    list.add(null)
    list.add(null)
    assertThat(list[0]).isEqualTo(null)
    assertThat(list[1]).isEqualTo(null)
    assertThat(list[2]).isEqualTo("A")
    assertThat(list[3]).isEqualTo(null)
    assertThat(list[4]).isEqualTo("B")
    assertThat(list[5]).isEqualTo(null)
    assertThat(list[6]).isEqualTo(null)
    assertThat(list[7]).isEqualTo(null)
    assertThat(list[8]).isEqualTo("C")
    assertThat(list[9]).isEqualTo(null)
    assertThat(list[10]).isEqualTo(null)
    assertThat(list.size).isEqualTo(11)
    assertThat(list.toList()).containsExactly(
      null, null, "A", null, "B", null, null, null, "C", null, null,
    )
  }

  @Test
  fun removeAtRemovesNull() {
    val list = SparseList<String?>()
    list.add(null)
    list.add(null)
    list.add("A")
    list.add(null)
    list.add(null)
    list.add("B")
    list.add(null)

    assertThat(list.removeAt(3)).isEqualTo(null)
    assertThat(list.toList()).containsExactly(null, null, "A", null, "B", null)

    assertThat(list.removeAt(3)).isEqualTo(null)
    assertThat(list.toList()).containsExactly(null, null, "A", "B", null)

    assertThat(list.removeAt(4)).isEqualTo(null)
    assertThat(list.toList()).containsExactly(null, null, "A", "B")

    assertThat(list.removeAt(0)).isEqualTo(null)
    assertThat(list.toList()).containsExactly(null, "A", "B")

    assertThat(list.removeAt(0)).isEqualTo(null)
    assertThat(list.toList()).containsExactly("A", "B")
  }

  @Test
  fun removeAtRemovesNonNull() {
    val list = SparseList<String?>()
    list.add(null)
    list.add(null)
    list.add("A")
    list.add(null)
    list.add(null)
    list.add("B")
    list.add(null)

    assertThat(list.removeAt(2)).isEqualTo("A")
    assertThat(list.toList()).containsExactly(null, null, null, null, "B", null)

    assertThat(list.removeAt(4)).isEqualTo("B")
    assertThat(list.toList()).containsExactly(null, null, null, null, null)
  }

  @Test
  fun removeAtRemovesOnlyNullElement() {
    val list = SparseList<String?>()
    list.add(null)

    assertThat(list.removeAt(0)).isEqualTo(null)
    assertThat(list.toList()).containsExactly()
  }

  @Test
  fun removeAtRemovesOnlyNonNullElement() {
    val list = SparseList<String?>()
    list.add("A")

    assertThat(list.removeAt(0)).isEqualTo("A")
    assertThat(list.toList()).containsExactly()
  }

  @Test
  fun addNullAtIndexWithIndexHit() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add(null)
    list.add("B")
    list.add(null)
    list.add(3, null)

    assertThat(list.size).isEqualTo(6)
    assertThat(list.toList()).containsExactly(null, "A", null, null, "B", null)
  }

  @Test
  fun addNullAtIndexWithIndexMiss() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add(null)
    list.add("B")
    list.add(null)
    list.add(2, null)

    assertThat(list.size).isEqualTo(6)
    assertThat(list.toList()).containsExactly(null, "A", null, null, "B", null)
  }

  @Test
  fun addNonNullAtIndexWithIndexHit() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add(null)
    list.add("B")
    list.add(null)
    list.add(3, "X")

    assertThat(list.size).isEqualTo(6)
    assertThat(list.toList()).containsExactly(null, "A", null, "X", "B", null)
  }

  @Test
  fun addNonNullAtIndexWithIndexMiss() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add(null)
    list.add("B")
    list.add(null)
    list.add(2, "X")

    assertThat(list.size).isEqualTo(6)
    assertThat(list.toList()).containsExactly(null, "A", "X", null, "B", null)
  }

  @Test
  fun addNullsBetweenNonNulls() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add("B")
    list.add(null)
    list.addNulls(2, 3)

    assertThat(list.size).isEqualTo(7)
    assertThat(list.toList()).containsExactly(null, "A", null, null, null, "B", null)
  }

  @Test
  fun addNullsBetweenNulls() {
    val list = SparseList<String?>()
    list.add("A")
    list.add(null)
    list.add(null)
    list.add("B")
    list.addNulls(2, 3)

    assertThat(list.size).isEqualTo(7)
    assertThat(list.toList()).containsExactly("A", null, null, null, null, null, "B")
  }

  @Test
  fun removeRangeRemovesNonNulls() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add("B")
    list.add("C")
    list.add(null)
    list.removeRange(1, 4)

    assertThat(list.size).isEqualTo(2)
    assertThat(list.toList()).containsExactly(null, null)
  }

  @Test
  fun removeRangeRemovesNulls() {
    val list = SparseList<String?>()
    list.add("A")
    list.add(null)
    list.add(null)
    list.add(null)
    list.add("B")
    list.removeRange(1, 4)

    assertThat(list.size).isEqualTo(2)
    assertThat(list.toList()).containsExactly("A", "B")
  }

  @Test
  fun removeRangeRemovesNullsAndNonNulls() {
    val list = SparseList<String?>()
    list.add(null)
    list.add("A")
    list.add(null)
    list.add(null)
    list.add("B")
    list.add(null)
    list.add("C")
    list.removeRange(1, 6)

    assertThat(list.size).isEqualTo(2)
    assertThat(list.toList()).containsExactly(null, "C")
  }

  @Test
  fun nonNullElements() {
    val list = SparseList<String?>()
    assertThat(list.nonNullElements).isEmpty()

    list.add(null)
    assertThat(list.nonNullElements).isEmpty()

    list.add("A")
    val a = list.nonNullElements
    assertThat(a).containsExactly("A")

    list.add(null)
    assertThat(list.nonNullElements).containsExactly("A")

    list.add("B")
    assertThat(list.nonNullElements).containsExactly("A", "B")
    assertThat(a).containsExactly("A") // Confirm each access returns a snapshot.
  }

  @Test
  fun getOrCreateCreates() {
    val list = SparseList<String?>()
    list.addNulls(0, 10)

    assertThat(list.getOrCreate(3) { "A" }).isEqualTo("A")
    assertThat(list[3]).isEqualTo("A")
  }

  @Test
  fun getOrCreateGets() {
    val list = SparseList<String?>()
    list.addNulls(0, 10)
    list.set(3, "A")
    assertThat(list.getOrCreate(3) { error("boom") }).isEqualTo("A")
  }
}
