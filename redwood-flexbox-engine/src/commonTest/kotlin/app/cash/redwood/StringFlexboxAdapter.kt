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

package app.cash.redwood

object StringFlexboxAdapter : FlexboxEngine.Adapter<StringWidget>() {
  override fun measure(
    node: Node<StringWidget>,
    widthSpecMode: MeasureSpecMode,
    width: Int,
    heightSpecMode: MeasureSpecMode,
    height: Int,
  ): Size {
    val stringNode = node.widget

    val lines = when (widthSpecMode) {
      MeasureSpecMode.Exactly -> stringNode.lines(maxWidth = width - 2)
      MeasureSpecMode.AtMost -> stringNode.lines(maxWidth = width - 2)
      else -> listOf(listOf(stringNode.text))
    }

    val measuredWidth = when (widthSpecMode) {
      MeasureSpecMode.Exactly -> width
      MeasureSpecMode.AtMost -> minOf(
        width,
        (lines.maxOfOrNull { line -> line.sumOf { word -> word.length + 1 } - 1 } ?: 0) + 2,
      )
      else -> stringNode.text.length + 2
    }

    val measuredHeight = when (heightSpecMode) {
      MeasureSpecMode.Exactly -> height
      MeasureSpecMode.AtMost -> minOf(height, lines.size + 2)
      else -> lines.size + 2
    }

    return Size(measuredWidth, measuredHeight)
  }

  override fun minWidth(node: Node<StringWidget>): Int {
    return node.widget.words.maxOf { it.length } + 2
  }

  override fun minHeight(node: Node<StringWidget>): Int {
    return 2
  }
}
