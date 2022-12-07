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
import kotlin.test.Test
import kotlin.test.assertEquals

class FlexContainerStringCanvasTest {
  private val imdbTop4 = listOf(
    "The Shawshank Redemption",
    "The Godfather",
    "The Dark Knight",
    "The Godfather Part II",
  )

  @Test
  fun column() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Column
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(14, 20, widgets),
    )
  }

  @Test
  fun columnMainAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Column
      justifyContent = JustifyContent.Center
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(20, 22, widgets),
    )
  }

  @Test
  fun columnCrossAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Column
      alignItems = AlignItems.Center
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(20, 20, widgets),
    )
  }

  @Test
  fun columnCrossAxisStretched() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Column
      alignItems = AlignItems.Stretch
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(20, 22, widgets),
    )
  }

  @Test
  fun row() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Row
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(60, 8, widgets),
    )
  }

  @Test
  fun rowMainAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Row
      justifyContent = JustifyContent.Center
      items += widgets.map { widget ->
        FlexItem(flexBasisPercent = 0.0, measurable = widget).also { widget.flexItem = it }
      }
      roundToInt = true
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
      container.layout(60, 6, widgets),
    )
  }

  @Test
  fun rowCrossAxisCentered() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Row
      alignItems = AlignItems.Center
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(42, 8, widgets),
    )
  }

  @Test
  fun rowCrossAxisStretched() {
    val widgets = imdbTop4.map { StringWidget(it) }
    val container = FlexContainer().apply {
      flexDirection = Row
      alignItems = AlignItems.Stretch
      items += widgets.map { it.flexItem }
      roundToInt = true
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
      container.layout(100, 8, widgets),
    )
  }

  private fun FlexContainer.layout(
    width: Int,
    height: Int,
    widgets: List<StringWidget>,
  ): String {
    val canvas = StringCanvas(width, height)
    measure(Constraints.fixed(width.toDouble(), height.toDouble()))

    for (widget in widgets) {
      widget.draw(canvas)
    }

    return canvas.toString()
  }
}
