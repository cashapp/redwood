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
import app.cash.redwood.widget.Widget
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class LazyListUpdateProcessorTest {
  private val processor = FakeUpdateProcessor()
    .apply {
      for (i in 0 until 10) {
        placeholder.insert(i, StringWidget("."))
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
    processor.items.insert(0, StringWidget("D"))
    processor.items.insert(1, StringWidget("E"))
    processor.items.insert(2, StringWidget("F"))
    processor.items.insert(3, StringWidget("G"))
    processor.items.insert(4, StringWidget("H"))
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
    processor.items.insert(0, StringWidget("A"))
    processor.items.insert(1, StringWidget("B"))
    processor.items.insert(2, StringWidget("C"))
    processor.items.insert(3, StringWidget("D"))
    processor.items.insert(4, StringWidget("E"))
    processor.onEndChanges()

    processor.scrollTo(0, 4)
    assertThat(processor.toString()).isEqualTo("A B C D [...8]")

    processor.itemsBefore(2)
    processor.itemsAfter(5)
    processor.items.insert(5, StringWidget("F"))
    processor.items.insert(6, StringWidget("G"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(". . C D [...8]")

    processor.itemsBefore(4)
    processor.itemsAfter(3)
    processor.items.insert(5, StringWidget("H"))
    processor.items.insert(6, StringWidget("I"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(". . . . [...8]")

    processor.itemsBefore(6)
    processor.itemsAfter(1)
    processor.items.insert(5, StringWidget("J"))
    processor.items.insert(6, StringWidget("K"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(". . . . [...8]")

    processor.itemsBefore(4)
    processor.itemsAfter(3)
    processor.items.insert(0, StringWidget("E"))
    processor.items.insert(1, StringWidget("F"))
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(". . . . [...8]")

    processor.itemsBefore(2)
    processor.itemsAfter(5)
    processor.items.insert(0, StringWidget("C"))
    processor.items.insert(1, StringWidget("D"))
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo(". . C D [...8]")

    processor.itemsBefore(0)
    processor.itemsAfter(7)
    processor.items.insert(0, StringWidget("A"))
    processor.items.insert(1, StringWidget("B"))
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("A B C D [...8]")
  }

  /**
   * We've got a fixed 4-element scroll position at the end of a dataset. Move the 5-element
   * loaded window across this space.
   */
  @Test
  fun moveLoadedWindowDownAndUpWithScrollPositionAtEnd() {
    processor.itemsBefore(0)
    processor.itemsAfter(7)
    processor.items.insert(0, StringWidget("A"))
    processor.items.insert(1, StringWidget("B"))
    processor.items.insert(2, StringWidget("C"))
    processor.items.insert(3, StringWidget("D"))
    processor.items.insert(4, StringWidget("E"))
    processor.onEndChanges()

    processor.scrollTo(8, 4)
    assertThat(processor.toString()).isEqualTo("[8...] . . . .")

    processor.itemsBefore(2)
    processor.itemsAfter(5)
    processor.items.insert(5, StringWidget("F"))
    processor.items.insert(6, StringWidget("G"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] . . . .")

    processor.itemsBefore(4)
    processor.itemsAfter(3)
    processor.items.insert(5, StringWidget("H"))
    processor.items.insert(6, StringWidget("I"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I . . .")

    processor.itemsBefore(6)
    processor.itemsAfter(1)
    processor.items.insert(5, StringWidget("J"))
    processor.items.insert(6, StringWidget("K"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J K .")

    processor.itemsBefore(7)
    processor.itemsAfter(0)
    processor.items.insert(5, StringWidget("L"))
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J K L")

    processor.itemsBefore(5)
    processor.itemsAfter(2)
    processor.items.insert(0, StringWidget("G"))
    processor.items.insert(1, StringWidget("H"))
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J . .")

    processor.itemsBefore(3)
    processor.itemsAfter(4)
    processor.items.insert(0, StringWidget("E"))
    processor.items.insert(1, StringWidget("F"))
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] . . . .")

    processor.itemsBefore(1)
    processor.itemsAfter(6)
    processor.items.insert(0, StringWidget("C"))
    processor.items.insert(1, StringWidget("D"))
    processor.items.remove(5, 1)
    processor.items.remove(5, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] . . . .")
  }

  @Test
  fun moveLoadedWindowDownAndUp2() {
    // Load the first 10.
    processor.itemsAfter(10)
    processor.items.insert(0, StringWidget("A"))
    processor.items.insert(1, StringWidget("B"))
    processor.items.insert(2, StringWidget("C"))
    processor.items.insert(3, StringWidget("D"))
    processor.items.insert(4, StringWidget("E"))
    processor.items.insert(5, StringWidget("F"))
    processor.items.insert(6, StringWidget("G"))
    processor.items.insert(7, StringWidget("H"))
    processor.items.insert(8, StringWidget("I"))
    processor.items.insert(9, StringWidget("J"))
    processor.onEndChanges()
    processor.scrollTo(8, 5)
    assertThat(processor.toString()).isEqualTo("[8...] I J . . . [...7]")

    // Load the middle 10.
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(10, StringWidget("K"))
    processor.items.insert(11, StringWidget("L"))
    processor.items.insert(12, StringWidget("M"))
    processor.items.insert(13, StringWidget("N"))
    processor.items.insert(14, StringWidget("O"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J K L M [...7]")

    // Load the bottom 10.
    processor.itemsBefore(10)
    processor.itemsAfter(0)
    processor.items.insert(10, StringWidget("P"))
    processor.items.insert(11, StringWidget("Q"))
    processor.items.insert(12, StringWidget("R"))
    processor.items.insert(13, StringWidget("S"))
    processor.items.insert(14, StringWidget("T"))
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.items.remove(0, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] . . K L M [...7]")

    // Load the middle 10.
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(0, StringWidget("F"))
    processor.items.insert(1, StringWidget("G"))
    processor.items.insert(2, StringWidget("H"))
    processor.items.insert(3, StringWidget("I"))
    processor.items.insert(4, StringWidget("J"))
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J K L M [...7]")

    // Load the first 10.
    processor.itemsBefore(0)
    processor.itemsAfter(10)
    processor.items.insert(0, StringWidget("A"))
    processor.items.insert(1, StringWidget("B"))
    processor.items.insert(2, StringWidget("C"))
    processor.items.insert(3, StringWidget("D"))
    processor.items.insert(4, StringWidget("E"))
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.items.remove(10, 1)
    processor.onEndChanges()
    assertThat(processor.toString()).isEqualTo("[8...] I J . . . [...7]")
  }

  @Test
  fun itemsBeforeGrowsAndShrinks() {
    processor.itemsBefore(5)
    processor.itemsAfter(5)
    processor.items.insert(0, StringWidget("F"))
    processor.items.insert(1, StringWidget("G"))
    processor.items.insert(2, StringWidget("H"))
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
    processor.items.insert(0, StringWidget("F"))
    processor.items.insert(1, StringWidget("G"))
    processor.items.insert(2, StringWidget("H"))
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

  class StringWidget(
    override var value: String,
  ) : Widget<String> {
    override var modifier: Modifier = Modifier
  }
}
