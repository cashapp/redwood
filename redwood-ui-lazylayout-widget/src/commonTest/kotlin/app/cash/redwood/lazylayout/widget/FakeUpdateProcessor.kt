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

import app.cash.redwood.lazylayout.widget.FakeUpdateProcessor.StringCell
import app.cash.redwood.lazylayout.widget.FakeUpdateProcessor.StringContent
import app.cash.redwood.widget.Widget

/**
 * This fake simulates a real scroll window, which is completely independent of the window of loaded
 * items. Tests should call [scrollTo] to move the scroll window.
 *
 * Interpreting [toString]
 * -----------------------
 *
 * This class encodes its UI state in a string for use in assert statements. This string is very
 * information-dense! Here's some samples:
 *
 *  * `A B C`
 *  * `[3...] . .v2 Fv2 G . [...4]`
 *
 * Here's how to make sense of this gibberish:
 *
 *  * `[3...]` - the number of elements before the top of the scroll window. This format doesn't
 *    care whether these elements are placeholders or loaded - they're off screen!
 *
 *  * `[...4]` - the number of elements after the bottom of the scroll window.
 *
 *  * `.` - an on-screen placeholder. This happens in customer apps when the scroll window has moved
 *    and the loaded window hasn't caught up to that yet.
 *
 *  * `A`, `B`, `C` - an on-screen loaded element. This can be any string but in our tests we
 *    typically put `A` at index 0, `B` at 1, etc.
 *
 *  * `@2` - how many times this cell has been updated in-place. When content is loaded or unloaded,
 *    the update processor can notify the UI with either a content edit (the cell's content changed)
 *    or a delete + insert. The two strategies yield the same content, but achieve it with different
 *    transition animations. We prefer edit-in-place over insert + delete when data loads.
 *
 * So given the update `[3...] . .v2 Fv2 G . [...4]`, we have a scroll window showing indexes 3 thru
 * 8 inclusive, with only 5 and 6 loaded. The elements at 4 and 5 have been updated-in-place, which
 * is why they're at version 2.
 */
class FakeUpdateProcessor : LazyListUpdateProcessor<StringCell, StringContent>() {
  private var dataSize = 0
  private var scrollWindowOffset = 0
  private val scrollWindowCells = mutableListOf<StringCell>()

  private fun getView(index: Int): StringCell {
    return getOrCreateView(index) { binding ->
      StringCell(binding)
    }
  }

  override fun insertRows(index: Int, count: Int) {
    require(index >= 0 && count >= 0 && index <= dataSize + 1)
    require(dataSize + count == super.size)
    dataSize += count

    for (i in index until index + count) {
      val scrollWindowLimit = scrollWindowOffset + scrollWindowCells.size
      if (i < scrollWindowOffset) {
        // Shift the scroll window down by one.
        scrollWindowOffset++
      } else if (i < scrollWindowLimit) {
        // Insert a new cell into the scroll window. We remove the last element to preserve the
        // scroll window's size.
        scrollWindowCells.add(i - scrollWindowOffset, getView(i))
        scrollWindowCells.removeLast().binding.unbind()
      }
    }
  }

  override fun deleteRows(index: Int, count: Int) {
    require(index >= 0 && count >= 0 && index + count <= dataSize)
    require(dataSize - count == super.size)
    dataSize -= count

    val originalScrollWindowSize = scrollWindowCells.size
    for (i in 0 until count) {
      val scrollWindowLimit = scrollWindowOffset + scrollWindowCells.size
      if (index < scrollWindowOffset) {
        // Shift the scroll window up by one.
        scrollWindowOffset--
      } else if (index < scrollWindowLimit) {
        // Delete a cell from the scroll window. We'll replenish the window below.
        val removed = scrollWindowCells.removeAt(index - scrollWindowOffset)
        require(!removed.binding.isPlaceholder && removed.content == null)
      }
    }

    // Preserve the scroll window's size if possible.
    // Note that this doesn't scroll up to force a constant size.
    while (
      scrollWindowCells.size < originalScrollWindowSize &&
      scrollWindowOffset + scrollWindowCells.size < dataSize
    ) {
      scrollWindowCells.add(getView(scrollWindowOffset + scrollWindowCells.size))
    }
  }

  override fun setContent(view: StringCell, widget: Widget<StringContent>?) {
    val content = widget?.value

    // It is an error for `content` to already have a parent cell.
    val previous = view.content
    require(content?.parentCell == null)
    if (previous != null) {
      previous.parentCell = null
    }
    if (content != null) {
      content.parentCell = view
    }

    view.version++
    view.content = content
  }

  fun scrollTo(offset: Int, count: Int) {
    val oldWindowCells = ArrayDeque(scrollWindowCells)
    var oldWindowCellsOffset = scrollWindowOffset

    scrollWindowOffset = offset
    scrollWindowCells.clear()

    // Recycle old cells that precede the new window.
    while (oldWindowCellsOffset < offset) {
      if (oldWindowCells.isNotEmpty()) oldWindowCells.removeFirst().binding.unbind()
      oldWindowCellsOffset++
    }

    // Populate the new window with either old or new cells.
    for (i in offset until offset + count) {
      if (i in oldWindowCellsOffset until oldWindowCellsOffset + oldWindowCells.size) {
        scrollWindowCells += oldWindowCells.removeFirst()
        oldWindowCellsOffset++
      } else {
        scrollWindowCells += getView(i)
      }
    }

    // Recycle old cells that are beyond the new window.
    while (oldWindowCells.isNotEmpty()) {
      oldWindowCells.removeFirst().binding.unbind()
    }
  }

  override fun toString(): String {
    val cellsBefore = scrollWindowOffset
    val cellsAfter = dataSize - cellsBefore - scrollWindowCells.size
    return scrollWindowCells.joinToString(
      prefix = when {
        cellsBefore > 0 -> "[$cellsBefore...] "
        else -> ""
      },
      postfix = when {
        cellsAfter > 0 -> " [...$cellsAfter]"
        else -> ""
      },
      separator = " ",
    ) {
      when {
        it.version != 1 -> "${it.content!!.value}v${it.version}"
        else -> it.content!!.value
      }
    }
  }

  class StringCell(
    val binding: Binding<StringCell, StringContent>,
  ) {
    var version = 0
    var content: StringContent? = null
  }

  class StringContent(
    var value: String,
  ) {
    internal var parentCell: StringCell? = null
  }
}
