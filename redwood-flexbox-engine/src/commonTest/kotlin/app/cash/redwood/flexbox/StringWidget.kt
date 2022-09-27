/*
 * Copyright (C) 2022 Square, Inc.
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
package app.cash.redwood.flexbox

/**
 * A string that wraps on space characters, and is wrapped with an ASCII-art box. We use a string
 * and not a fixed-size box because it works more like real widgets.
 */
class StringWidget(
  private val text: String,
) : Measurable() {
  private val words = text.split(" ")

  private var left = -1
  private var top = -1
  private var right = -1
  private var bottom = -1

  override val minWidth
    get() = words.maxOf { it.length } + 2
  override val minHeight
    get() = 2

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec): Size {
    val widthSpecMode = widthSpec.mode
    val width = widthSpec.size
    val heightSpecMode = heightSpec.mode
    val height = heightSpec.size

    val lines = when (widthSpecMode) {
      MeasureSpecMode.Exactly -> lines(maxWidth = width - 2)
      MeasureSpecMode.AtMost -> lines(maxWidth = width - 2)
      else -> listOf(listOf(text))
    }

    val measuredWidth = when (widthSpecMode) {
      MeasureSpecMode.Exactly -> width
      MeasureSpecMode.AtMost -> minOf(
        width,
        (lines.maxOfOrNull { line -> line.sumOf { word -> word.length + 1 } - 1 } ?: 0) + 2,
      )
      else -> text.length + 2
    }

    val measuredHeight = when (heightSpecMode) {
      MeasureSpecMode.Exactly -> height
      MeasureSpecMode.AtMost -> minOf(height, lines.size + 2)
      else -> lines.size + 2
    }

    return Size(measuredWidth, measuredHeight)
  }

  internal fun layout(left: Int, top: Int, right: Int, bottom: Int) {
    this.left = left
    this.top = top
    this.right = right
    this.bottom = bottom
  }

  /** Returns a list of rows, each containing the words of that row. */
  private fun lines(maxWidth: Int): List<List<String>> {
    var i = 0
    val result = mutableListOf<List<String>>()

    // Build each row.
    while (i < words.size) {
      val row = mutableListOf<String>()
      result += row
      var rowWidth = 0

      // Collect the words in this row. Each row gets at least one word!
      while (i < words.size) {
        val word = words[i]
        if (rowWidth == 0) {
          row += word
          rowWidth = word.length
          i++
        } else if (rowWidth + 1 + word.length <= maxWidth) {
          row += word
          rowWidth += 1 + word.length
          i++
        } else {
          break
        }
      }
    }

    return result
  }

  fun draw(canvas: StringCanvas) {
    val lines = lines(maxWidth = right - left - 2)

    var y = top

    if (y < bottom) {
      drawHorizontalLine(canvas, '┌', '┐', y, left, right)
      y++
    }

    for (line in lines) {
      if (y < bottom) {
        drawWords(canvas, line, y, left, right)
      }
      y++
    }

    while (y < bottom - 1) {
      drawWords(canvas, listOf(), y, left, right)
      y++
    }

    if (top < bottom) {
      drawHorizontalLine(canvas, '└', '┘', bottom - 1, left, right)
    }
  }

  private fun drawHorizontalLine(
    canvas: StringCanvas,
    leftCorner: Char,
    rightCorner: Char,
    y: Int,
    left: Int,
    right: Int,
  ) {
    if (left < right) {
      canvas[left, y] = leftCorner
      canvas[right - 1, y] = rightCorner
    }
    for (i in (left + 1) until (right - 1)) {
      canvas[i, y] = '─'
    }
  }

  private fun drawWords(
    canvas: StringCanvas,
    words: List<String>,
    y: Int,
    left: Int,
    right: Int,
  ) {
    var x = left

    if (x < right) {
      canvas[left, y] = '|'
      x++
    }

    for ((w, word) in words.withIndex()) {
      if (x >= right) break

      if (w > 0) {
        canvas[x++, y] = ' '
      }

      for (c in word.toCharArray()) {
        if (x < right) {
          canvas[x++, y] = c
        }
      }
    }

    while (x < right - 1) {
      canvas[x++, y] = ' '
    }

    if (left < right) {
      canvas[right - 1, y] = '│'
    }
  }
}
