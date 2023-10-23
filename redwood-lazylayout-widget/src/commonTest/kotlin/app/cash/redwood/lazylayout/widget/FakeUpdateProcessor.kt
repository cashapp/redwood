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

import app.cash.redwood.widget.Widget

/**
 * This fake simulates a real scroll window, which is completely independent of the window of loaded
 * items. Tests should call [scrollTo] to move the scroll window.
 */
class FakeUpdateProcessor : LazyListUpdateProcessor<FakeUpdateProcessor.StringCell, String>() {
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
        scrollWindowCells.removeAt(index - scrollWindowOffset).binding.unbind()
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

  override fun setContent(view: StringCell, content: Widget<String>?) {
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
      it.content!!.value
    }
  }

  class StringCell(
    val binding: Binding<StringCell, String>,
  ) {
    var content: Widget<String>? = null
  }
}
