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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FlexboxEngineTest {

  private val engine = FlexboxEngine().apply {
    flexWrap = FlexWrap.Wrap
    alignItems = AlignItems.Stretch
    alignContent = AlignContent.Stretch
  }

  @Test
  fun testCalculateHorizontalFlexLines() {
    val node1 = Node(measurable = BoxMeasurable(100, 100))
    val node2 = Node(measurable = BoxMeasurable(200, 100))
    val node3 = Node(measurable = BoxMeasurable(300, 100))
    val node4 = Node(measurable = BoxMeasurable(400, 100))
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexWrap = FlexWrap.Wrap
    val widthMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)

    engine.ensureIndexToFlexLine(engine.nodes.size)
    val flexLines = engine.calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)

    assertEquals(3, flexLines.size)
    assertEquals(300, flexLines[0].mainSize)
    assertEquals(300, flexLines[1].mainSize)
    assertEquals(400, flexLines[2].mainSize)
    assertEquals(100, flexLines[0].crossSize)
    assertEquals(100, flexLines[1].crossSize)
    assertEquals(100, flexLines[2].crossSize)

    assertNotNull(engine.indexToFlexLine)
    assertEquals(0, engine.indexToFlexLine!![0])
    assertEquals(0, engine.indexToFlexLine!![1])
    assertEquals(1, engine.indexToFlexLine!![2])
    assertEquals(2, engine.indexToFlexLine!![3])

    val firstLine = flexLines[0]
    assertEquals(0, firstLine.firstIndex)
    assertEquals(1, firstLine.lastIndex)
    val secondLine = flexLines[1]
    assertEquals(2, secondLine.firstIndex)
    assertEquals(2, secondLine.lastIndex)
    val thirdLine = flexLines[2]
    assertEquals(3, thirdLine.firstIndex)
    assertEquals(3, thirdLine.lastIndex)
  }

  @Test
  fun testCalculateVerticalFlexLines() {
    val node1 = Node(measurable = BoxMeasurable(100, 100))
    val node2 = Node(measurable = BoxMeasurable(100, 200))
    val node3 = Node(measurable = BoxMeasurable(100, 300))
    val node4 = Node(measurable = BoxMeasurable(100, 400))
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexWrap = FlexWrap.Wrap
    engine.flexDirection = FlexDirection.Column
    val widthMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    val heightMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)

    engine.ensureIndexToFlexLine(engine.nodes.size)
    val flexLines = engine.calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)

    assertEquals(3, flexLines.size)
    assertEquals(300, flexLines[0].mainSize)
    assertEquals(300, flexLines[1].mainSize)
    assertEquals(400, flexLines[2].mainSize)
    assertEquals(100, flexLines[0].crossSize)
    assertEquals(100, flexLines[1].crossSize)
    assertEquals(100, flexLines[2].crossSize)

    assertNotNull(engine.indexToFlexLine)
    assertEquals(0, engine.indexToFlexLine!![0])
    assertEquals(0, engine.indexToFlexLine!![1])
    assertEquals(1, engine.indexToFlexLine!![2])
    assertEquals(2, engine.indexToFlexLine!![3])

    val firstLine = flexLines[0]
    assertEquals(0, firstLine.firstIndex)
    assertEquals(1, firstLine.lastIndex)
    val secondLine = flexLines[1]
    assertEquals(2, secondLine.firstIndex)
    assertEquals(2, secondLine.lastIndex)
    val thirdLine = flexLines[2]
    assertEquals(3, thirdLine.firstIndex)
    assertEquals(3, thirdLine.lastIndex)
  }

  @Test
  fun testDetermineMainSize_direction_row_flexGrowSet() {
    val node1 = Node(measurable = BoxMeasurable(100, 100))
    val node2 = Node(measurable = BoxMeasurable(200, 100), flexGrow = 1.0f)
    val node3 = Node(measurable = BoxMeasurable(300, 100))
    val node4 = Node(measurable = BoxMeasurable(400, 100), flexGrow = 2.0f)
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexDirection = FlexDirection.Row
    engine.flexWrap = FlexWrap.Wrap
    val widthMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    engine.flexLines = engine.calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)

    assertEquals(100, node1.measuredWidth)
    assertEquals(100, node1.measuredHeight)
    // node2 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(400, node2.measuredWidth)
    assertEquals(100, node2.measuredHeight)
    assertEquals(300, node3.measuredWidth)
    assertEquals(100, node3.measuredHeight)
    // node4 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(500, node4.measuredWidth)
    assertEquals(100, node4.measuredHeight)
  }

  @Test
  fun testDetermineMainSize_direction_column_flexGrowSet() {
    val node1 = Node(measurable = BoxMeasurable(100, 100))
    val node2 = Node(measurable = BoxMeasurable(100, 200), flexGrow = 1.0f)
    val node3 = Node(measurable = BoxMeasurable(100, 300))
    val node4 = Node(measurable = BoxMeasurable(100, 400), flexGrow = 2.0f)
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexDirection = FlexDirection.Column
    engine.flexWrap = FlexWrap.Wrap
    val widthMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    val heightMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    engine.flexLines = engine.calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)

    assertEquals(100, node1.measuredWidth)
    assertEquals(100, node1.measuredHeight)
    assertEquals(100, node2.measuredWidth)
    // node2 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(400, node2.measuredHeight)
    assertEquals(100, node3.measuredWidth)
    assertEquals(300, node3.measuredHeight)
    assertEquals(100, node4.measuredWidth)
    // node4 will expand to fill the left space in the first flex line since flex grow is set
    assertEquals(500, node4.measuredHeight)
  }

  @Test
  fun testDetermineMainSize_direction_row_flexShrinkSet() {
    val node1 = Node(measurable = BoxMeasurable(200, 100))
    val node2 = Node(measurable = BoxMeasurable(200, 100))
    val node3 = Node(measurable = BoxMeasurable(200, 100))
    val node4 = Node(measurable = BoxMeasurable(200, 100))
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexDirection = FlexDirection.Row
    engine.flexWrap = FlexWrap.NoWrap
    val widthMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    engine.flexLines = engine.calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)

    // Flex shrink is set to 1.0 (default value) for all views.
    // They should be shrank equally for the amount overflown the width
    assertEquals(125, node1.measuredWidth)
    assertEquals(100, node1.measuredHeight)
    assertEquals(125, node2.measuredWidth)
    assertEquals(100, node2.measuredHeight)
    assertEquals(125, node3.measuredWidth)
    assertEquals(100, node3.measuredHeight)
    assertEquals(125, node4.measuredWidth)
    assertEquals(100, node4.measuredHeight)
  }

  @Test
  fun testDetermineMainSize_direction_column_flexShrinkSet() {
    val node1 = Node(measurable = BoxMeasurable(100, 200))
    val node2 = Node(measurable = BoxMeasurable(100, 200))
    val node3 = Node(measurable = BoxMeasurable(100, 200))
    val node4 = Node(measurable = BoxMeasurable(100, 200))
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexDirection = FlexDirection.Column
    engine.flexWrap = FlexWrap.NoWrap
    val widthMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    val heightMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    engine.flexLines = engine.calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)

    // Flex shrink is set to 1.0 (default value) for all views.
    // They should be shrank equally for the amount overflown the height
    assertEquals(100, node1.measuredWidth)
    assertEquals(125, node1.measuredHeight)
    assertEquals(100, node2.measuredWidth)
    assertEquals(125, node2.measuredHeight)
    assertEquals(100, node3.measuredWidth)
    assertEquals(125, node3.measuredHeight)
    assertEquals(100, node4.measuredWidth)
    assertEquals(125, node4.measuredHeight)
  }

  @Test
  fun testDetermineMainSize_directionRow_fixedSizeViewAndShrinkable_doNotExceedMaxMainSize() {
    val node1 = Node(measurable = BoxMeasurable(100, 100), flexShrink = 0f)
    val node2 = Node(measurable = BoxMeasurable(2000, 2000)) // simulate a very long text view
    engine.nodes += node1
    engine.nodes += node2
    engine.flexWrap = FlexWrap.NoWrap
    val widthMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.AtMost)
    val heightMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    engine.flexLines = engine.calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)

    // Container with WRAP_CONTENT and a max width forces resizable children to shrink
    // to avoid exceeding max available space.
    assertEquals(100, node1.measuredWidth)
    assertEquals(400, node2.measuredWidth)
  }

  @Test
  fun testDetermineMainSize_directionRow_twoFixedSizeViewsAndShrinkable_doNotExceedMaxMainSize() {
    val node1 = Node(measurable = BoxMeasurable(100, 100), flexShrink = 0f)
    val node2 = Node(measurable = BoxMeasurable(2000, 2000)) // simulate a very long text view
    val node3 = Node(measurable = BoxMeasurable(100, 100), flexShrink = 0f)
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.flexWrap = FlexWrap.NoWrap
    val widthMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.AtMost)
    val heightMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Unspecified)
    engine.flexLines = engine.calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)

    // Container with WRAP_CONTENT and a max width forces resizable children to shrink
    // to avoid exceeding max available space.
    assertEquals(100, node1.measuredWidth)
    assertEquals(300, node2.measuredWidth)
    assertEquals(100, node3.measuredWidth)
  }

  @Test
  fun testDetermineCrossSize_direction_row_alignContent_stretch() {
    val node1 = Node(measurable = BoxMeasurable(100, 100))
    val node2 = Node(measurable = BoxMeasurable(200, 100))
    val node3 = Node(measurable = BoxMeasurable(300, 100))
    val node4 = Node(measurable = BoxMeasurable(400, 100))
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexDirection = FlexDirection.Row
    engine.flexWrap = FlexWrap.Wrap
    engine.alignContent = AlignContent.Stretch
    val widthMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Exactly)
    engine.flexLines = engine.calculateHorizontalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)
    engine.determineCrossSize(widthMeasureSpec, heightMeasureSpec, 0)
    engine.stretchChildren()

    // align content is set to Align.STRETCH, the cross size for each flex line is stretched
    // to distribute the remaining free space along the cross axis
    // (remaining height in this case)
    assertEquals(333, node1.measuredHeight)
    assertEquals(333, node2.measuredHeight)
    assertEquals(333, node3.measuredHeight)
    assertEquals(334, node4.measuredHeight)
  }

  @Test
  fun testDetermineCrossSize_direction_column_alignContent_stretch() {
    val node1 = Node(measurable = BoxMeasurable(100, 100))
    val node2 = Node(measurable = BoxMeasurable(100, 200))
    val node3 = Node(measurable = BoxMeasurable(100, 300))
    val node4 = Node(measurable = BoxMeasurable(100, 400))
    engine.nodes += node1
    engine.nodes += node2
    engine.nodes += node3
    engine.nodes += node4
    engine.flexDirection = FlexDirection.Column
    engine.flexWrap = FlexWrap.Wrap
    engine.alignContent = AlignContent.Stretch
    val widthMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    engine.flexLines = engine.calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    engine.determineMainSize(widthMeasureSpec, heightMeasureSpec)
    engine.determineCrossSize(widthMeasureSpec, heightMeasureSpec, 0)
    engine.stretchChildren()

    // align content is set to Align.STRETCH, the cross size for each flex line is stretched
    // to distribute the remaining free space along the cross axis
    // (remaining width in this case)
    assertEquals(333, node1.measuredWidth)
    assertEquals(333, node2.measuredWidth)
    assertEquals(333, node3.measuredWidth)
    assertEquals(334, node4.measuredWidth)
  }

  @Test
  fun testMakeCombinedLong() {
    var higher = -1
    var lower = 10
    var combined = packLong(lower, higher)
    assertEquals(higher, unpackHigher(combined))
    assertEquals(lower, unpackLower(combined))

    higher = Int.MAX_VALUE
    lower = Int.MIN_VALUE
    combined = packLong(lower, higher)
    assertEquals(higher, unpackHigher(combined))
    assertEquals(lower, unpackLower(combined))

    higher = MeasureSpec.from(500, MeasureSpecMode.Exactly).value
    lower = MeasureSpec.from(300, MeasureSpecMode.Unspecified).value
    combined = packLong(lower, higher)
    assertEquals(higher, unpackHigher(combined))
    assertEquals(lower, unpackLower(combined))
  }

  @Test
  fun testMakeMeasureSpec() {
    var spec = MeasureSpec.from(100, MeasureSpecMode.AtMost)
    assertEquals(100, spec.size)
    assertEquals(MeasureSpecMode.AtMost, spec.mode)

    spec = MeasureSpec.from(999, MeasureSpecMode.Exactly)
    assertEquals(999, spec.size)
    assertEquals(MeasureSpecMode.Exactly, spec.mode)

    spec = MeasureSpec.from(0, MeasureSpecMode.Unspecified)
    assertEquals(0, spec.size)
    assertEquals(MeasureSpecMode.Unspecified, spec.mode)
  }

  @Test
  fun testFlexLine_anyItemsHaveFlexGrow() {
    val node1 = Node(measurable = BoxMeasurable(100, 100), flexGrow = 1.0f)
    val node2 = Node(measurable = BoxMeasurable(100, 200))
    val node3 = Node(measurable = BoxMeasurable(100, 300))
    val node4 = Node(measurable = BoxMeasurable(100, 400), flexGrow = 2.0f)
    engine.apply {
      nodes += node1
      nodes += node2
      nodes += node3
      nodes += node4
      flexDirection = FlexDirection.Column
      flexWrap = FlexWrap.Wrap
      alignContent = AlignContent.Stretch
    }
    val widthMeasureSpec = MeasureSpec.from(1000, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(500, MeasureSpecMode.Exactly)
    engine.flexLines = engine.calculateVerticalFlexLines(widthMeasureSpec, heightMeasureSpec)
    assertEquals(3, engine.flexLines.size)
    assertTrue(engine.flexLines[0].anyItemsHaveFlexGrow)
    assertFalse(engine.flexLines[1].anyItemsHaveFlexGrow)
    assertTrue(engine.flexLines[2].anyItemsHaveFlexGrow)
  }

  class BoxMeasurable(
    override val width: Int,
    override val height: Int,
  ) : Measurable()
}
