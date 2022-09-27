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

import app.cash.redwood.flexbox.FlexDirection.Companion.Column
import app.cash.redwood.flexbox.FlexDirection.Companion.Row
import app.cash.redwood.flexbox.FlexDirection.Companion.RowReverse
import kotlin.test.Test
import kotlin.test.assertEquals

class FlexboxTest {
  private val imdbTop4 = listOf(
    "The Shawshank Redemption",
    "The Godfather",
    "The Dark Knight",
    "The Godfather Part II",
  )

  @Test
  fun column() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Column
      nodes += widgets.map { it.toNode() }
    }

    assertEquals(
      """
      ┌──────────┐··
      |The       │··
      |Shawshank │··
      |Redemption│··
      └──────────┘··
      ┌─────────┐···
      |The      │···
      |Godfather│···
      └─────────┘···
      ┌────────┐····
      |The Dark│····
      |Knight  │····
      └────────┘····
      ┌─────────┐···
      |The      │···
      |Godfather│···
      |Part II  │···
      └─────────┘···
      ··············
      ··············
      """.trimIndent(),
      engine.layout(14, 20, widgets),
    )
  }

  @Test
  fun columnCrossAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Column
      justifyContent = JustifyContent.Center
      nodes += widgets.map { it.toNode() }
    }

    assertEquals(
      """
      ····················
      ····················
      ····················
      ····················
      ┌─────────────┐·····
      |The Shawshank│·····
      |Redemption   │·····
      └─────────────┘·····
      ┌─────────────┐·····
      |The Godfather│·····
      └─────────────┘·····
      ┌───────────────┐···
      |The Dark Knight│···
      └───────────────┘···
      ┌──────────────────┐
      |The Godfather Part│
      |II                │
      └──────────────────┘
      ····················
      ····················
      ····················
      ····················
      """.trimIndent(),
      engine.layout(20, 22, widgets),
    )
  }

  @Test
  fun columnMainAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Column
      alignItems = AlignItems.Center
      nodes += widgets.map { it.toNode() }
    }

    assertEquals(
      """
      ··┌─────────────┐···
      ··|The Shawshank│···
      ··|Redemption   │···
      ··└─────────────┘···
      ··┌─────────────┐···
      ··|The Godfather│···
      ··└─────────────┘···
      ·┌───────────────┐··
      ·|The Dark Knight│··
      ·└───────────────┘··
      ┌──────────────────┐
      |The Godfather Part│
      |II                │
      └──────────────────┘
      ····················
      ····················
      ····················
      ····················
      ····················
      ····················
      """.trimIndent(),
      engine.layout(20, 20, widgets),
    )
  }

  @Test
  fun columnCrossAxisStretched() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Column
      alignItems = AlignItems.Stretch
      nodes += widgets.map { it.toNode() }
    }

    assertEquals(
      """
      ┌──────────────────┐
      |The Shawshank     │
      |Redemption        │
      └──────────────────┘
      ┌──────────────────┐
      |The Godfather     │
      └──────────────────┘
      ┌──────────────────┐
      |The Dark Knight   │
      └──────────────────┘
      ┌──────────────────┐
      |The Godfather Part│
      |II                │
      └──────────────────┘
      ····················
      ····················
      ····················
      ····················
      ····················
      ····················
      ····················
      ····················
      """.trimIndent(),
      engine.layout(20, 22, widgets),
    )
  }

  @Test
  fun row() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Row
      nodes += widgets.map { it.toNode() }
    }

    assertEquals(
      """
      ┌───────────────────┐┌─────────┐┌──────────┐┌──────────────┐
      |The Shawshank      │|The      │|The Dark  │|The Godfather │
      |Redemption         │|Godfather│|Knight    │|Part II       │
      └───────────────────┘└─────────┘└──────────┘└──────────────┘
      ····························································
      ····························································
      ····························································
      ····························································
      """.trimIndent(),
      engine.layout(60, 8, widgets),
    )
  }

  @Test
  fun rowCrossAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Row
      nodes += widgets.map { it.toNode() }
      alignItems = AlignItems.Center
    }

    assertEquals(
      """
      ··········································
      ┌──────────┐···········┌──────┐┌─────────┐
      |The       │┌─────────┐|The   │|The      │
      |Shawshank │|The      │|Dark  │|Godfather│
      |Redemption│|Godfather│|Knight│|Part II  │
      └──────────┘└─────────┘└──────┘└─────────┘
      ··········································
      ··········································
      """.trimIndent(),
      engine.layout(42, 8, widgets),
    )
  }

  @Test
  fun rowMainAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Row
      nodes += widgets.map { it.toNode(FlexNode(flexBasisPercent = 0f)) }
      justifyContent = JustifyContent.Center
    }

    assertEquals(
      """
      ·········┌──────────┐┌─────────┐┌──────┐┌─────────┐·········
      ·········|The       │|The      │|The   │|The      │·········
      ·········|Shawshank │|Godfather│|Dark  │|Godfather│·········
      ·········|Redemption│└─────────┘|Knight│|Part II  │·········
      ·········└──────────┘···········└──────┘|         │·········
      ········································└─────────┘·········
      """.trimIndent(),
      engine.layout(60, 6, widgets),
    )
  }

  @Test
  fun rowCrossAxisStretched() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val engine = FlexboxEngine().apply {
      flexDirection = Row
      alignItems = AlignItems.Stretch
      nodes += widgets.map { it.toNode() }
    }

    assertEquals(
      """
      ┌────────────────────────┐┌─────────────┐┌───────────────┐┌─────────────────────┐···················
      |The Shawshank Redemption│|The Godfather│|The Dark Knight│|The Godfather Part II│···················
      |                        │|             │|               │|                     │···················
      |                        │|             │|               │|                     │···················
      |                        │|             │|               │|                     │···················
      |                        │|             │|               │|                     │···················
      |                        │|             │|               │|                     │···················
      └────────────────────────┘└─────────────┘└───────────────┘└─────────────────────┘···················
      """.trimIndent(),
      engine.layout(100, 8, widgets),
    )
  }

  private fun FlexboxEngine.layout(
    width: Int,
    height: Int,
    widgets: List<StringWidget>,
  ): String {
    val canvas = StringCanvas(width, height)
    val widthSpec = MeasureSpec.from(width, MeasureSpecMode.Exactly)
    val heightSpec = MeasureSpec.from(height, MeasureSpecMode.Exactly)

    flexLines = when (flexDirection) {
      Row, RowReverse -> calculateHorizontalFlexLines(widthSpec, heightSpec)
      else -> calculateVerticalFlexLines(widthSpec, heightSpec)
    }

    measure(widthSpec, heightSpec)
    layout(0, 0, width, height)

    for (widget in widgets) {
      widget.draw(canvas)
    }

    return canvas.toString()
  }

  private fun StringWidget.toNode(node: FlexNode = FlexNode()): FlexNode {
    return node.apply {
      this.measurable = this@toNode
      this.layout = this@toNode::layout
    }
  }
}
