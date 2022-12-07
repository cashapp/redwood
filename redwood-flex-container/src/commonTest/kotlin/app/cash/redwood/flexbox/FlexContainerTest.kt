/*
 * Copyright 2016 Google Inc. All rights reserved.
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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FlexContainerTest {

  private val container = FlexContainer().apply {
    flexWrap = FlexWrap.Wrap
    alignItems = AlignItems.Stretch
    alignContent = AlignContent.Stretch
    roundToInt = true
  }

  @Test
  fun testCalculateHorizontalFlexLines() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(200.0, 100.0))
    val item3 = FlexItem(measurable = BoxMeasurable(300.0, 100.0))
    val item4 = FlexItem(measurable = BoxMeasurable(400.0, 100.0))
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Row
    container.flexWrap = FlexWrap.Wrap

    val constraints = Constraints.fixedWidth(500.0)
    val lines = container.calculateFlexLines(constraints)

    assertEquals(3, lines.size)
    assertEquals(300.0, lines[0].mainSize)
    assertEquals(300.0, lines[1].mainSize)
    assertEquals(400.0, lines[2].mainSize)
    assertEquals(100.0, lines[0].crossSize)
    assertEquals(100.0, lines[1].crossSize)
    assertEquals(100.0, lines[2].crossSize)

    val firstLine = lines[0]
    assertEquals(0, firstLine.firstIndex)
    assertEquals(1, firstLine.lastIndex)
    val secondLine = lines[1]
    assertEquals(2, secondLine.firstIndex)
    assertEquals(2, secondLine.lastIndex)
    val thirdLine = lines[2]
    assertEquals(3, thirdLine.firstIndex)
    assertEquals(3, thirdLine.lastIndex)
  }

  @Test
  fun testCalculateVerticalFlexLines() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 300.0))
    val item4 = FlexItem(measurable = BoxMeasurable(100.0, 400.0))
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Column
    container.flexWrap = FlexWrap.Wrap

    val constraints = Constraints.fixedHeight(500.0)
    val lines = container.calculateFlexLines(constraints)

    assertEquals(3, lines.size)
    assertEquals(300.0, lines[0].mainSize)
    assertEquals(300.0, lines[1].mainSize)
    assertEquals(400.0, lines[2].mainSize)
    assertEquals(100.0, lines[0].crossSize)
    assertEquals(100.0, lines[1].crossSize)
    assertEquals(100.0, lines[2].crossSize)

    val firstLine = lines[0]
    assertEquals(0, firstLine.firstIndex)
    assertEquals(1, firstLine.lastIndex)
    val secondLine = lines[1]
    assertEquals(2, secondLine.firstIndex)
    assertEquals(2, secondLine.lastIndex)
    val thirdLine = lines[2]
    assertEquals(3, thirdLine.firstIndex)
    assertEquals(3, thirdLine.lastIndex)
  }

  @Test
  fun testDetermineMainSize_direction_row_flexGrowSet() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(200.0, 100.0), flexGrow = 1.0)
    val item3 = FlexItem(measurable = BoxMeasurable(300.0, 100.0))
    val item4 = FlexItem(measurable = BoxMeasurable(400.0, 100.0), flexGrow = 2.0)
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Row
    container.flexWrap = FlexWrap.Wrap

    val constraints = Constraints.fixedWidth(500.0)
    container.measure(constraints)

    assertEquals(100.0, item1.width)
    assertEquals(100.0, item1.height)
    // item2 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(400.0, item2.width)
    assertEquals(100.0, item2.height)
    assertEquals(300.0, item3.width)
    assertEquals(100.0, item3.height)
    // item4 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(500.0, item4.width)
    assertEquals(100.0, item4.height)
  }

  @Test
  fun testDetermineMainSize_direction_column_flexGrowSet() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(100.0, 200.0), flexGrow = 1.0)
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 300.0))
    val item4 = FlexItem(measurable = BoxMeasurable(100.0, 400.0), flexGrow = 2.0)
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Column
    container.flexWrap = FlexWrap.Wrap

    val constraints = Constraints.fixedHeight(500.0)
    container.measure(constraints)

    assertEquals(100.0, item1.width)
    assertEquals(100.0, item1.height)
    assertEquals(100.0, item2.width)
    // item2 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(400.0, item2.height)
    assertEquals(100.0, item3.width)
    assertEquals(300.0, item3.height)
    assertEquals(100.0, item4.width)
    // item4 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(500.0, item4.height)
  }

  @Test
  fun testDetermineMainSize_direction_row_flexShrinkSet() {
    val item1 = FlexItem(measurable = BoxMeasurable(200.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(200.0, 100.0))
    val item3 = FlexItem(measurable = BoxMeasurable(200.0, 100.0))
    val item4 = FlexItem(measurable = BoxMeasurable(200.0, 100.0))
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Row
    container.flexWrap = FlexWrap.NoWrap

    val constraints = Constraints.fixedWidth(500.0)
    container.measure(constraints)

    // Flex shrink is set to 1.0 (default value) for all views.
    // They should be shrunk equally for the amount overflown the width
    assertEquals(125.0, item1.width)
    assertEquals(100.0, item1.height)
    assertEquals(125.0, item2.width)
    assertEquals(100.0, item2.height)
    assertEquals(125.0, item3.width)
    assertEquals(100.0, item3.height)
    assertEquals(125.0, item4.width)
    assertEquals(100.0, item4.height)
  }

  @Test
  fun testDetermineMainSize_direction_column_flexShrinkSet() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    val item2 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    val item4 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Column
    container.flexWrap = FlexWrap.NoWrap

    val constraints = Constraints.fixedHeight(500.0)
    container.measure(constraints)

    // Flex shrink is set to 1.0 (default value) for all views.
    // They should be shrunk equally for the amount overflown the height
    assertEquals(100.0, item1.width)
    assertEquals(125.0, item1.height)
    assertEquals(100.0, item2.width)
    assertEquals(125.0, item2.height)
    assertEquals(100.0, item3.width)
    assertEquals(125.0, item3.height)
    assertEquals(100.0, item4.width)
    assertEquals(125.0, item4.height)
  }

  @Test
  fun testDetermineMainSize_directionRow_fixedSizeViewAndShrinkable_doNotExceedMaxMainSize() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexShrink = 0.0)
    val item2 = FlexItem(measurable = BoxMeasurable(2000.0, 2000.0)) // simulate a very long text view
    container.items += item1
    container.items += item2
    container.flexWrap = FlexWrap.NoWrap

    val constraints = Constraints(maxWidth = 500.0)
    container.measure(constraints)

    // Container with WRAP_CONTENT and a max width forces resizable children to shrink
    // to avoid exceeding max available space.
    assertEquals(100.0, item1.width)
    assertEquals(400.0, item2.width)
  }

  @Test
  fun testDetermineMainSize_directionRow_twoFixedSizeViewsAndShrinkable_doNotExceedMaxMainSize() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexShrink = 0.0)
    val item2 = FlexItem(measurable = BoxMeasurable(2000.0, 2000.0)) // simulate a very long text view
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexShrink = 0.0)
    container.items += item1
    container.items += item2
    container.items += item3
    container.flexWrap = FlexWrap.NoWrap

    val constraints = Constraints(maxWidth = 500.0)
    container.measure(constraints)

    // Container with WRAP_CONTENT and a max width forces resizable children to shrink
    // to avoid exceeding max available space.
    assertEquals(100.0, item1.width)
    assertEquals(300.0, item2.width)
    assertEquals(100.0, item3.width)
  }

  @Test
  fun testDetermineCrossSize_direction_row_alignContent_stretch() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(200.0, 100.0))
    val item3 = FlexItem(measurable = BoxMeasurable(300.0, 100.0))
    val item4 = FlexItem(measurable = BoxMeasurable(400.0, 100.0))
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Row
    container.flexWrap = FlexWrap.Wrap
    container.alignContent = AlignContent.Stretch

    val constraints = Constraints.fixed(500.0, 1000.0)
    container.measure(constraints)

    // align content is set to Align.STRETCH, the cross size for each flex line is stretched
    // to distribute the remaining free space along the cross axis
    // (remaining height in this case)
    assertEquals(333.0, item1.height)
    assertEquals(333.0, item2.height)
    assertEquals(333.0, item3.height)
    assertEquals(334.0, item4.height)
  }

  @Test
  fun testDetermineCrossSize_direction_column_alignContent_stretch() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0))
    val item2 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 300.0))
    val item4 = FlexItem(measurable = BoxMeasurable(100.0, 400.0))
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Column
    container.flexWrap = FlexWrap.Wrap
    container.alignContent = AlignContent.Stretch

    val constraints = Constraints.fixed(1000.0, 500.0)
    container.measure(constraints)

    // align content is set to Align.STRETCH, the cross size for each flex line is stretched
    // to distribute the remaining free space along the cross axis
    // (remaining width in this case)
    assertEquals(333.0, item1.width)
    assertEquals(333.0, item2.width)
    assertEquals(333.0, item3.width)
    assertEquals(334.0, item4.width)
  }

  @Test
  fun testFlexLine_anyItemsHaveFlexGrow() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexGrow = 1.0)
    val item2 = FlexItem(measurable = BoxMeasurable(100.0, 200.0))
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 300.0))
    val item4 = FlexItem(measurable = BoxMeasurable(100.0, 400.0), flexGrow = 2.0)
    container.items += item1
    container.items += item2
    container.items += item3
    container.items += item4
    container.flexDirection = FlexDirection.Column
    container.flexWrap = FlexWrap.Wrap
    container.alignContent = AlignContent.Stretch

    val constraints = Constraints.fixed(1000.0, 500.0)
    val lines = container.calculateFlexLines(constraints)

    assertEquals(3, lines.size)
    assertTrue(lines[0].anyItemsHaveFlexGrow)
    assertFalse(lines[1].anyItemsHaveFlexGrow)
    assertTrue(lines[2].anyItemsHaveFlexGrow)
  }

  class BoxMeasurable(
    override val requestedWidth: Double,
    override val requestedHeight: Double,
  ) : Measurable()
}
