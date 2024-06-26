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
import kotlin.test.assertFailsWith

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

  @Test
  fun addRange() {
    val source = SparseList<String?>()
    source.addNulls(0, 8)
    source.set(4, "A")
    source.set(5, "B")
    source.set(7, "C")

    val target = SparseList<String?>()
    target.addNulls(0, 8)
    target.set(2, "D")
    target.set(3, "E")
    target.set(6, "F")
    assertThat(target.toList())
      .containsExactly(null, null, "D", "E", null, null, "F", null)

    // Add [null, "A", "B", null, "C"] before "E".
    target.addRange(3, source, sourceIndex = 3, count = 5)

    assertThat(target.toList())
      .containsExactly(null, null, "D", null, "A", "B", null, "C", "E", null, null, "F", null)
  }

  @Test
  fun addRangeSourceElementsAllNull() {
    val source = SparseList<String?>()
    source.addNulls(0, 5)
    source.set(0, "A")
    source.set(4, "B")

    val target = SparseList<String?>()
    target.addNulls(0, 8)
    target.set(2, "D")
    target.set(3, "E")
    assertThat(target.toList())
      .containsExactly(null, null, "D", "E", null, null, null, null)

    // Add [null, null, null] before "E".
    target.addRange(3, source, sourceIndex = 1, count = 3)

    assertThat(target.toList())
      .containsExactly(null, null, "D", null, null, null, "E", null, null, null, null)
  }

  @Test
  fun addRangeSourceElementsAllNonNull() {
    val source = SparseList<String?>()
    source.addNulls(0, 5)
    source.set(0, "A")
    source.set(1, "B")
    source.set(2, "C")
    source.set(3, "D")
    source.set(4, "E")

    val target = SparseList<String?>()
    target.addNulls(0, 8)
    target.set(2, "F")
    target.set(3, "G")
    assertThat(target.toList())
      .containsExactly(null, null, "F", "G", null, null, null, null)

    // Add ["B", "C", "D"] before "G".
    target.addRange(3, source, sourceIndex = 1, count = 3)

    assertThat(target.toList())
      .containsExactly(null, null, "F", "B", "C", "D", "G", null, null, null, null)
  }

  @Test
  fun addRangeToFront() {
    val source = SparseList<String?>()
    source.addNulls(0, 5)
    source.set(0, "A")
    source.set(1, "B")
    source.set(2, "C")
    source.set(3, "D")
    source.set(4, "E")

    val target = SparseList<String?>()
    target.addNulls(0, 5)
    target.set(2, "F")
    target.set(3, "G")
    assertThat(target.toList())
      .containsExactly(null, null, "F", "G", null)

    // Add ["B", "C", "D"] at 0.
    target.addRange(0, source, sourceIndex = 1, count = 3)

    assertThat(target.toList())
      .containsExactly("B", "C", "D", null, null, "F", "G", null)
  }

  @Test
  fun addRangeToEnd() {
    val source = SparseList<String?>()
    source.addNulls(0, 5)
    source.set(0, "A")
    source.set(1, "B")
    source.set(2, "C")
    source.set(3, "D")
    source.set(4, "E")

    val target = SparseList<String?>()
    target.addNulls(0, 5)
    target.set(2, "F")
    target.set(3, "G")
    assertThat(target.toList())
      .containsExactly(null, null, "F", "G", null)

    // Add ["B", "C", "D"] at 5.
    target.addRange(5, source, sourceIndex = 1, count = 3)

    assertThat(target.toList())
      .containsExactly(null, null, "F", "G", null, "B", "C", "D")
  }

  /**
   * This test highlights the difference between operating on logical indices vs. physical indices.
   * This is the kind of thing we expect to be efficient in benchmarks.
   */
  @Test
  fun addRangeWithLargeSparseIndexes() {
    val source = SparseList<String?>()
    source.addNulls(0, 9_000)
    source.set(5_000, "A")
    source.set(5_001, "B")
    source.set(5_003, "C")

    val target = SparseList<String?>()
    target.addNulls(0, 7_000)
    target.set(3_001, "D")
    target.set(3_002, "E")
    target.set(3_005, "F")

    target.addRange(3_004, source, sourceIndex = 4_000, count = 2_000)

    val expectedSource = buildList {
      for (i in 0 until 9_000) add(null)
      set(5_000, "A")
      set(5_001, "B")
      set(5_003, "C")
    }
    val expected = buildList {
      for (i in 0 until 7_000) add(null)
      set(3_001, "D")
      set(3_002, "E")
      set(3_005, "F")
      addAll(3_004, expectedSource.subList(4_000, 4_000 + 2_000))
    }

    assertThat(target.toList()).isEqualTo(expected)
  }

  @Test
  fun addOutOfBounds() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.add(1, "A")
    }
  }

  @Test
  fun addNegativeIndex() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.add(-1, "A")
    }
  }

  @Test
  fun addNullOutOfBounds() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.add(1, null)
    }
  }

  @Test
  fun addNullNegativeIndex() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.add(-1, null)
    }
  }

  @Test
  fun addRangeOfValuesWithTargetIndexOutOfBounds() {
    val source = SparseList<String?>()
    source.add("A")

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(1, source, 0, 1)
    }
  }

  @Test
  fun addRangeOfValuesWithTargetIndexNegative() {
    val source = SparseList<String?>()
    source.add("A")

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(-1, source, 0, 1)
    }
  }

  @Test
  fun addRangeOfNullsWithTargetIndexOutOfBounds() {
    val source = SparseList<String?>()
    source.add(null)

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(1, source, 0, 1)
    }
  }

  @Test
  fun addRangeOfNullsWithTargetIndexNegative() {
    val source = SparseList<String?>()
    source.add(null)

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(-1, source, 0, 1)
    }
  }

  @Test
  fun addRangeWithSourceCountOutOfBounds() {
    val source = SparseList<String?>()

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(0, source, 0, 1)
    }
  }

  @Test
  fun addRangeWithSourceCountNegative() {
    val source = SparseList<String?>()

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(0, source, 0, -1)
    }
  }

  @Test
  fun addRangeOfNullsWithSourceIndexOutOfBounds() {
    val source = SparseList<String?>()
    source.add(null)

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(0, source, 1, 1)
    }
  }

  @Test
  fun addRangeOfNullsWithSourceIndexNegative() {
    val source = SparseList<String?>()
    source.add(null)

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(0, source, 1, -1)
    }
  }

  @Test
  fun addRangeOfNullsWithSourceCountOutOfBounds() {
    val source = SparseList<String?>()
    source.add(null)

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(0, source, 0, 2)
    }
  }

  @Test
  fun addRangeOfNullsWithSourceCountNegative() {
    val source = SparseList<String?>()
    source.add(null)

    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addRange(0, source, 0, -1)
    }
  }

  @Test
  fun addNullsIndexOutOfBounds() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addNulls(1, 1)
    }
  }

  @Test
  fun addNullsIndexNegative() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addNulls(-1, 1)
    }
  }

  @Test
  fun addNullsCountNegative() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.addNulls(0, -1)
    }
  }

  @Test
  fun removeAtOutOfBounds() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.removeAt(1)
    }
  }

  @Test
  fun removeAtNegativeIndex() {
    val list = SparseList<String?>()
    assertFailsWith<IllegalArgumentException> {
      list.removeAt(-1)
    }
  }

  @Test
  fun removeRangeOutOfBounds() {
    val list = SparseList<String?>()
    list.add("A")
    assertFailsWith<IllegalArgumentException> {
      list.removeRange(1, 2)
    }
  }

  @Test
  fun removeRangeNegativeIndex() {
    val list = SparseList<String?>()
    list.add("A")
    assertFailsWith<IllegalArgumentException> {
      list.removeRange(-1, 2)
    }
  }

  @Test
  fun removeRangeNegativeCount() {
    val list = SparseList<String?>()
    list.add("A")
    assertFailsWith<IllegalArgumentException> {
      list.removeRange(0, -1)
    }
  }

  @Test
  fun removeRangeWithNullOutOfBounds() {
    val list = SparseList<String?>()
    list.add(null)
    assertFailsWith<IllegalArgumentException> {
      list.removeRange(1, 2)
    }
  }

  @Test
  fun removeRangeWithNullNegativeIndex() {
    val list = SparseList<String?>()
    list.add(null)
    assertFailsWith<IllegalArgumentException> {
      list.removeRange(-1, 1)
    }
  }

  @Test
  fun removeRangeWithNullNegativeCount() {
    val list = SparseList<String?>()
    list.add(null)
    assertFailsWith<IllegalArgumentException> {
      list.removeRange(0, -1)
    }
  }
}
