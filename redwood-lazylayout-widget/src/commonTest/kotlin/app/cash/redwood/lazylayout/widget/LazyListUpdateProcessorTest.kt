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

import app.cash.redwood.Modifier
import app.cash.redwood.lazylayout.widget.FakeUpdateProcessor.StringContent
import app.cash.redwood.widget.Widget
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class LazyListUpdateProcessorTest {
  private val processor = FakeUpdateProcessor()
    .apply {
      for (i in 0 until 10) {
        placeholder.insert(i, ".")
      }
    }

  @Test
  fun allPlaceholders() {
    processor.itemsBefore(0)
    processor.itemsAfter(10)
    processor.onEndChanges()

    processor.scrollTo(0, 10)
    assertThat(processor.toString()).isEqualTo(". . . . . . . . . .")
  }

  @Test
  fun initiallyEmpty() {
    processor.itemsBefore(0)
    processor.itemsAfter(0)
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("")
  }

  /**
   * We've got a fixed set of loaded data:
   *
   * . . . D E F G H . . . .
   *
   * Scroll a 5-element window up and down over this region and confirm we see the loaded data.
   */
  @Test
  fun moveScrollWindowDownAndUp() {
    processor.itemsBefore(3)
    processor.itemsAfter(4)
    processor.items.insert(0, "D")
    processor.items.insert(1, "E")
    processor.items.insert(2, "F")
    processor.items.insert(3, "G")
    processor.items.insert(4, "H")
    processor.onEndChanges()

    processor.scrollTo(0, 5)
    assertThat(processor.toString()).isEqualTo(". . . D E [...7]")

    processor.scrollTo(2, 5)
    assertThat(processor.toString()).isEqualTo("[2...] . D E F G [...5]")

    processor.scrollTo(4, 5)
    assertThat(processor.toString()).isEqualTo("[4...] E F G H . [...3]")

    processor.scrollTo(6, 5)
    assertThat(processor.toString()).isEqualTo("[6...] G H . . . [...1]")

    processor.scrollTo(7, 5)
    assertThat(processor.toString()).isEqualTo("[7...] H . . . .")

    processor.scrollTo(6, 5)
    assertThat(processor.toString()).isEqualTo("[6...] G H . . . [...1]")

    processor.scrollTo(4, 5)
    assertThat(processor.toString()).isEqualTo("[4...] E F G H . [...3]")

    processor.scrollTo(2, 5)
    assertThat(processor.toString()).isEqualTo("[2...] . D E F G [...5]")

    processor.scrollTo(0, 5)
    assertThat(processor.toString()).isEqualTo(". . . D E [...7]")
  }

  /**
   * We've got a fixed 4-element scroll position at the front of a dataset. Move the 5-element
   * loaded window across this space.
   */
  @Test
  fun moveLoadedWindowDownAndUpWithScrollPositionAtFront() {
    processor.itemsBefore(0)
    processor.itemsAfter(7)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.items.insert(3, "D")
    processor.items.insert(4, "E")
    processor.onEndChanges()

    processor.scrollTo(0, 4)
    assertThat(processor.toString()).isEqualTo("A B C D [...8]")

    processor.itemsBefore(2)
    processor.itemsAfter(5)
    processor.items.insert(5, "F")
    processor.items.insert(6, "G")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(".v2 .v2 C D [...8]")

    processor.itemsBefore(4)
    processor.itemsAfter(3)
    processor.items.insert(5, "H")
    processor.items.insert(6, "I")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(".v2 .v2 .v2 .v2 [...8]")

    processor.itemsBefore(6)
    processor.itemsAfter(1)
    processor.items.insert(5, "J")
    processor.items.insert(6, "K")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(".v2 .v2 .v2 .v2 [...8]")

    processor.itemsBefore(4)
    processor.itemsAfter(3)
    processor.items.insert(0, "E")
    processor.items.insert(1, "F")
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(".v2 .v2 .v2 .v2 [...8]")

    processor.itemsBefore(2)
    processor.itemsAfter(5)
    processor.items.insert(0, "C")
    processor.items.insert(1, "D")
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(".v2 .v2 Cv3 Dv3 [...8]")

    processor.itemsBefore(0)
    processor.itemsAfter(7)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("Av3 Bv3 Cv3 Dv3 [...8]")
  }

  /**
   * We've got a fixed 4-element scroll position at the end of a dataset. Move the 5-element
   * loaded window across this space.
   */
  @Test
  fun moveLoadedWindowDownAndUpWithScrollPositionAtEnd() {
    processor.itemsBefore(0)
    processor.itemsAfter(7)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.items.insert(3, "D")
    processor.items.insert(4, "E")
    processor.onEndChanges()

    processor.scrollTo(8, 4)
    assertThat(processor.toString()).isEqualTo("[8...] . . . .")

    processor.itemsBefore(2)
    processor.itemsAfter(5)
    processor.items.insert(5, "F")
    processor.items.insert(6, "G")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] . . . .")

    processor.itemsBefore(4)
    processor.itemsAfter(3)
    processor.items.insert(5, "H")
    processor.items.insert(6, "I")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] Iv2 . . .")

    processor.itemsBefore(6)
    processor.itemsAfter(1)
    processor.items.insert(5, "J")
    processor.items.insert(6, "K")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] Iv2 Jv2 Kv2 .")

    processor.itemsBefore(7)
    processor.itemsAfter(0)
    processor.items.insert(5, "L")
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] Iv2 Jv2 Kv2 Lv2")

    processor.itemsBefore(5)
    processor.itemsAfter(2)
    processor.items.insert(0, "G")
    processor.items.insert(1, "H")
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] Iv2 Jv2 .v3 .v3")

    processor.itemsBefore(3)
    processor.itemsAfter(4)
    processor.items.insert(0, "E")
    processor.items.insert(1, "F")
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] .v3 .v3 .v3 .v3")

    processor.itemsBefore(1)
    processor.itemsAfter(6)
    processor.items.insert(0, "C")
    processor.items.insert(1, "D")
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] .v3 .v3 .v3 .v3")
  }

  @Test
  fun moveLoadedWindowDownAndUp2() {
    // Load the first 10.
    processor.itemsAfter(10)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.items.insert(3, "D")
    processor.items.insert(4, "E")
    processor.items.insert(5, "F")
    processor.items.insert(6, "G")
    processor.items.insert(7, "H")
    processor.items.insert(8, "I")
    processor.items.insert(9, "J")
    processor.onEndChanges()
    processor.scrollTo(8, 5)
    assertThat(processor.toString()).isEqualTo("[8...] I J . . . [...7]")

    // Load the middle 10.
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(10, "K")
    processor.items.insert(11, "L")
    processor.items.insert(12, "M")
    processor.items.insert(13, "N")
    processor.items.insert(14, "O")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J Kv2 Lv2 Mv2 [...7]")

    // Load the bottom 10.
    processor.itemsBefore(10)
    processor.itemsAfter(0)
    processor.items.insert(10, "P")
    processor.items.insert(11, "Q")
    processor.items.insert(12, "R")
    processor.items.insert(13, "S")
    processor.items.insert(14, "T")
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] .v2 .v2 Kv2 Lv2 Mv2 [...7]")

    // Load the middle 10.
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(0, "F")
    processor.items.insert(1, "G")
    processor.items.insert(2, "H")
    processor.items.insert(3, "I")
    processor.items.insert(4, "J")
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] Iv3 Jv3 Kv2 Lv2 Mv2 [...7]")

    // Load the first 10.
    processor.itemsBefore(0)
    processor.itemsAfter(10)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.items.insert(3, "D")
    processor.items.insert(4, "E")
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] Iv3 Jv3 .v3 .v3 .v3 [...7]")
  }

  @Test
  fun itemsBeforeGrowsAndShrinks() {
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(0, "F")
    processor.items.insert(1, "G")
    processor.items.insert(2, "H")
    processor.onEndChanges()

    processor.scrollTo(4, 5)
    assertThat(processor.toString()).isEqualTo("[4...] . F G H . [...4]")

    processor.itemsBefore(2)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[1...] . F G H . [...4]")

    processor.itemsBefore(5)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[4...] . F G H . [...4]")
  }

  @Test
  fun itemsAfterGrowsAndShrinks() {
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(0, "F")
    processor.items.insert(1, "G")
    processor.items.insert(2, "H")
    processor.onEndChanges()

    processor.scrollTo(4, 5)
    assertThat(processor.toString()).isEqualTo("[4...] . F G H . [...4]")

    processor.itemsAfter(2)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[4...] . F G H . [...1]")

    processor.itemsAfter(5)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[4...] . F G H . [...4]")
  }

  @Test
  fun noncontiguousScrollUp() {
    processor.itemsBefore(4)
    processor.itemsAfter(4)
    processor.items.insert(0, "E")
    processor.items.insert(1, "F")
    processor.items.insert(2, "G")
    processor.onEndChanges()

    processor.scrollTo(4, 3)
    assertThat(processor.toString()).isEqualTo("[4...] E F G [...4]")

    processor.scrollTo(0, 3)
    assertThat(processor.toString()).isEqualTo(". . . [...8]")

    processor.itemsBefore(0)
    processor.itemsAfter(8)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.items.remove(3, 3)
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("Av2 Bv2 Cv2 [...8]")
  }

  @Test
  fun noncontiguousScrollDown() {
    processor.itemsBefore(4)
    processor.itemsAfter(4)
    processor.items.insert(0, "E")
    processor.items.insert(1, "F")
    processor.items.insert(2, "G")
    processor.onEndChanges()

    processor.scrollTo(4, 3)
    assertThat(processor.toString()).isEqualTo("[4...] E F G [...4]")

    processor.scrollTo(8, 3)
    assertThat(processor.toString()).isEqualTo("[8...] . . .")

    processor.itemsBefore(8)
    processor.itemsAfter(0)
    processor.items.insert(0, "I")
    processor.items.insert(1, "J")
    processor.items.insert(2, "K")
    processor.items.remove(3, 3)
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("[8...] Iv2 Jv2 Kv2")
  }

  @Test
  fun adjacentScrollUp() {
    processor.itemsBefore(3)
    processor.itemsAfter(3)
    processor.items.insert(0, "D")
    processor.items.insert(1, "E")
    processor.items.insert(2, "F")
    processor.onEndChanges()

    processor.scrollTo(3, 3)
    assertThat(processor.toString()).isEqualTo("[3...] D E F [...3]")

    processor.scrollTo(0, 3)
    assertThat(processor.toString()).isEqualTo(". . . [...6]")

    processor.itemsBefore(0)
    processor.itemsAfter(6)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.items.remove(3, 3)
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("Av2 Bv2 Cv2 [...6]")
  }

  @Test
  fun adjacentScrollDown() {
    processor.itemsBefore(3)
    processor.itemsAfter(3)
    processor.items.insert(0, "D")
    processor.items.insert(1, "E")
    processor.items.insert(2, "F")
    processor.onEndChanges()

    processor.scrollTo(3, 3)
    assertThat(processor.toString()).isEqualTo("[3...] D E F [...3]")

    processor.scrollTo(6, 3)
    assertThat(processor.toString()).isEqualTo("[6...] . . .")

    processor.itemsBefore(6)
    processor.itemsAfter(0)
    processor.items.insert(0, "G")
    processor.items.insert(1, "H")
    processor.items.insert(2, "I")
    processor.items.remove(3, 3)
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("[6...] Gv2 Hv2 Iv2")
  }

  /** Our event processing should work regardless of the order changes are received in. */
  @Test
  fun noncontiguousScrollUpRemoveChangeFirst() {
    processor.itemsBefore(4)
    processor.itemsAfter(4)
    processor.items.insert(0, "E")
    processor.items.insert(1, "F")
    processor.items.insert(2, "G")
    processor.onEndChanges()

    processor.scrollTo(4, 3)
    assertThat(processor.toString()).isEqualTo("[4...] E F G [...4]")

    processor.scrollTo(0, 3)
    assertThat(processor.toString()).isEqualTo(". . . [...8]")

    processor.itemsBefore(0)
    processor.itemsAfter(8)
    processor.items.remove(0, 3)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.items.insert(2, "C")
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("Av2 Bv2 Cv2 [...8]")
  }

  @Test
  fun noncontiguousScrollDownRemoveChangeFirst() {
    processor.itemsBefore(4)
    processor.itemsAfter(4)
    processor.items.insert(0, "E")
    processor.items.insert(1, "F")
    processor.items.insert(2, "G")
    processor.onEndChanges()

    processor.scrollTo(4, 3)
    assertThat(processor.toString()).isEqualTo("[4...] E F G [...4]")

    processor.scrollTo(8, 3)
    assertThat(processor.toString()).isEqualTo("[8...] . . .")

    processor.itemsBefore(8)
    processor.itemsAfter(0)
    processor.items.remove(0, 3)
    processor.items.insert(0, "I")
    processor.items.insert(1, "J")
    processor.items.insert(2, "K")
    processor.onEndChanges()

    assertThat(processor.toString()).isEqualTo("[8...] Iv2 Jv2 Kv2")
  }

  @Test
  fun removeMiddleLoadedItemThatIsBound() {
    processor.itemsBefore(8)
    processor.itemsAfter(8)
    processor.items.insert(0, "I")
    processor.items.insert(1, "J")
    processor.items.insert(2, "K")
    processor.items.insert(3, "L")
    processor.onEndChanges()

    processor.scrollTo(6, 8)
    assertThat(processor.toString()).isEqualTo("[6...] . . I J K L . . [...6]")

    processor.items.remove(2, 1) // 'K'.
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[6...] . . I J L . . . [...5]")
  }

  @Test
  fun removeMiddleLoadedItemThatIsNotBound() {
    processor.itemsBefore(8)
    processor.itemsAfter(8)
    processor.items.insert(0, "I")
    processor.items.insert(1, "J")
    processor.items.insert(2, "K")
    processor.items.insert(3, "L")
    processor.onEndChanges()

    processor.scrollTo(2, 4)
    assertThat(processor.toString()).isEqualTo("[2...] . . . . [...14]")

    processor.items.remove(2, 1) // 'K'.
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[2...] . . . . [...13]")
  }

  /**
   * We had a bug where we attempted to move the split between `itemsBefore` and `itemsAfter` beyond
   * the end of `itemsAfter`. This bug would occurs when the following held:
   *
   *  * The items before count is shrinking
   *  * The insert size is greater than all unloaded items (itemsBefore + itemsAfter)
   *
   * https://github.com/cashapp/redwood/issues/2172
   */
  @Test
  fun shiftItemsFromAfterToBeforeWhenThereAreNotEnoughToShift() {
    processor.itemsBefore(3)
    processor.itemsAfter(3)
    processor.onEndChanges()
    processor.scrollTo(0, 6)
    assertThat(processor.toString()).isEqualTo(". . . . . .")

    processor.itemsBefore(2)
    processor.items.insert(0, "C")
    processor.items.insert(1, "D")
    processor.items.insert(2, "E")
    processor.items.insert(3, "F")
    processor.items.insert(4, "G")
    processor.items.insert(5, "H")
    processor.items.insert(6, "I")
    processor.onEndChanges()
    processor.scrollTo(0, 12)
    assertThat(processor.toString()).isEqualTo(". . C D E Fv2 G H I . . .")
  }

  @Test
  fun shiftItemsFromAfterToBeforeWhenAfterIsEmpty() {
    processor.itemsBefore(1)
    processor.itemsAfter(0)
    processor.onEndChanges()
    processor.scrollTo(0, 1)
    assertThat(processor.toString()).isEqualTo(".")

    processor.itemsBefore(0)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.onEndChanges()
    processor.scrollTo(0, 2)
    assertThat(processor.toString()).isEqualTo("A B")
  }

  @Test
  fun shiftItemsFromBeforeToAfterWhenThereAreNotEnoughToShift() {
    processor.itemsBefore(3)
    processor.itemsAfter(3)
    processor.onEndChanges()
    processor.scrollTo(0, 6)
    assertThat(processor.toString()).isEqualTo(". . . . . .")

    processor.items.insert(0, "C")
    processor.items.insert(1, "D")
    processor.items.insert(2, "E")
    processor.items.insert(3, "F")
    processor.items.insert(4, "G")
    processor.items.insert(5, "H")
    processor.items.insert(6, "I")
    processor.itemsAfter(2)
    processor.onEndChanges()
    processor.scrollTo(0, 12)
    assertThat(processor.toString()).isEqualTo(". . . Cv2 D E F G H I . .")
  }

  @Test
  fun shiftItemsFromBeforeToAfterWhenBeforeIsEmpty() {
    processor.itemsBefore(0)
    processor.itemsAfter(1)
    processor.onEndChanges()
    processor.scrollTo(0, 1)
    assertThat(processor.toString()).isEqualTo(".")

    processor.itemsAfter(0)
    processor.items.insert(0, "A")
    processor.items.insert(1, "B")
    processor.onEndChanges()
    processor.scrollTo(0, 2)
    assertThat(processor.toString()).isEqualTo("Av2 B")
  }

  private fun Widget.Children<StringContent>.insert(index: Int, value: String) {
    val content = StringContent(value)
    val widget = object : Widget<StringContent> {
      override val value = content
      override var modifier: Modifier = Modifier
    }
    insert(index, widget)
  }
}
