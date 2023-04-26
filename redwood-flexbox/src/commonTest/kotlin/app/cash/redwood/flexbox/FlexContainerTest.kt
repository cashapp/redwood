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

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

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
    val widthMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)

    val lines = container.calculateFlexLines(widthMeasureSpec, heightMeasureSpec)

    assertThat(lines).hasSize(3)
    assertThat(lines[0].mainSize).isEqualTo(300.0)
    assertThat(lines[1].mainSize).isEqualTo(300.0)
    assertThat(lines[2].mainSize).isEqualTo(400.0)
    assertThat(lines[0].crossSize).isEqualTo(100.0)
    assertThat(lines[1].crossSize).isEqualTo(100.0)
    assertThat(lines[2].crossSize).isEqualTo(100.0)

    val firstLine = lines[0]
    assertThat(firstLine.firstIndex).isEqualTo(0)
    assertThat(firstLine.lastIndex).isEqualTo(1)
    val secondLine = lines[1]
    assertThat(secondLine.firstIndex).isEqualTo(2)
    assertThat(secondLine.lastIndex).isEqualTo(2)
    val thirdLine = lines[2]
    assertThat(thirdLine.firstIndex).isEqualTo(3)
    assertThat(thirdLine.lastIndex).isEqualTo(3)
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
    val widthMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    val heightMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)

    val lines = container.calculateFlexLines(widthMeasureSpec, heightMeasureSpec)

    assertThat(lines).hasSize(3)
    assertThat(lines[0].mainSize).isEqualTo(300.0)
    assertThat(lines[1].mainSize).isEqualTo(300.0)
    assertThat(lines[2].mainSize).isEqualTo(400.0)
    assertThat(lines[0].crossSize).isEqualTo(100.0)
    assertThat(lines[1].crossSize).isEqualTo(100.0)
    assertThat(lines[2].crossSize).isEqualTo(100.0)

    val firstLine = lines[0]
    assertThat(firstLine.firstIndex).isEqualTo(0)
    assertThat(firstLine.lastIndex).isEqualTo(1)
    val secondLine = lines[1]
    assertThat(secondLine.firstIndex).isEqualTo(2)
    assertThat(secondLine.lastIndex).isEqualTo(2)
    val thirdLine = lines[2]
    assertThat(thirdLine.firstIndex).isEqualTo(3)
    assertThat(thirdLine.lastIndex).isEqualTo(3)
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
    val widthMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    assertThat(item1.width).isEqualTo(100.0)
    assertThat(item1.height).isEqualTo(100.0)
    // item2 will expand to fill the left space in the first flex line since flex grow is set
    assertThat(item2.width).isEqualTo(400.0)
    assertThat(item2.height).isEqualTo(100.0)
    assertThat(item3.width).isEqualTo(300.0)
    assertThat(item3.height).isEqualTo(100.0)
    // item4 will expand to fill the left space in the first flex line since flex grow is set
    assertThat(item4.width).isEqualTo(500.0)
    assertThat(item4.height).isEqualTo(100.0)
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
    val widthMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    val heightMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    assertThat(item1.width).isEqualTo(100.0)
    assertThat(item1.height).isEqualTo(100.0)
    assertThat(item2.width).isEqualTo(100.0)
    // item2 will expand to fill the left space in the first flex line since flex grow is set
    assertThat(item2.height).isEqualTo(400.0)
    assertThat(item3.width).isEqualTo(100.0)
    assertThat(item3.height).isEqualTo(300.0)
    assertThat(item4.width).isEqualTo(100.0)
    // item4 will expand to fill the left space in the first flex line since flex grow is set
    assertThat(item4.height).isEqualTo(500.0)
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
    val widthMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    // Flex shrink is set to 1.0 (default value) for all views.
    // They should be shrunk equally for the amount overflown the width
    assertThat(item1.width).isEqualTo(125.0)
    assertThat(item1.height).isEqualTo(100.0)
    assertThat(item2.width).isEqualTo(125.0)
    assertThat(item2.height).isEqualTo(100.0)
    assertThat(item3.width).isEqualTo(125.0)
    assertThat(item3.height).isEqualTo(100.0)
    assertThat(item4.width).isEqualTo(125.0)
    assertThat(item4.height).isEqualTo(100.0)
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
    val widthMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    val heightMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    // Flex shrink is set to 1.0 (default value) for all views.
    // They should be shrunk equally for the amount overflown the height
    assertThat(item1.width).isEqualTo(100.0)
    assertThat(item1.height).isEqualTo(125.0)
    assertThat(item2.width).isEqualTo(100.0)
    assertThat(item2.height).isEqualTo(125.0)
    assertThat(item3.width).isEqualTo(100.0)
    assertThat(item3.height).isEqualTo(125.0)
    assertThat(item4.width).isEqualTo(100.0)
    assertThat(item4.height).isEqualTo(125.0)
  }

  @Test
  fun testDetermineMainSize_directionRow_fixedSizeViewAndShrinkable_doNotExceedMaxMainSize() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexShrink = 0.0)
    val item2 =
      FlexItem(measurable = BoxMeasurable(2000.0, 2000.0)) // simulate a very long text view
    container.items += item1
    container.items += item2
    container.flexWrap = FlexWrap.NoWrap
    val widthMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.AtMost)
    val heightMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    // Container with WRAP_CONTENT and a max width forces resizable children to shrink
    // to avoid exceeding max available space.
    assertThat(item1.width).isEqualTo(100.0)
    assertThat(item2.width).isEqualTo(400.0)
  }

  @Test
  fun testDetermineMainSize_directionRow_twoFixedSizeViewsAndShrinkable_doNotExceedMaxMainSize() {
    val item1 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexShrink = 0.0)
    val item2 =
      FlexItem(measurable = BoxMeasurable(2000.0, 2000.0)) // simulate a very long text view
    val item3 = FlexItem(measurable = BoxMeasurable(100.0, 100.0), flexShrink = 0.0)
    container.items += item1
    container.items += item2
    container.items += item3
    container.flexWrap = FlexWrap.NoWrap
    val widthMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.AtMost)
    val heightMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Unspecified)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    // Container with WRAP_CONTENT and a max width forces resizable children to shrink
    // to avoid exceeding max available space.
    assertThat(item1.width).isEqualTo(100.0)
    assertThat(item2.width).isEqualTo(300.0)
    assertThat(item3.width).isEqualTo(100.0)
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
    val widthMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Exactly)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    // align content is set to Align.STRETCH, the cross size for each flex line is stretched
    // to distribute the remaining free space along the cross axis
    // (remaining height in this case)
    assertThat(item1.height).isEqualTo(333.0)
    assertThat(item2.height).isEqualTo(333.0)
    assertThat(item3.height).isEqualTo(333.0)
    assertThat(item4.height).isEqualTo(334.0)
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
    val widthMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    container.measure(widthMeasureSpec, heightMeasureSpec)

    // align content is set to Align.STRETCH, the cross size for each flex line is stretched
    // to distribute the remaining free space along the cross axis
    // (remaining width in this case)
    assertThat(item1.width).isEqualTo(333.0)
    assertThat(item2.width).isEqualTo(333.0)
    assertThat(item3.width).isEqualTo(333.0)
    assertThat(item4.width).isEqualTo(334.0)
  }

  @Test
  fun testMakeMeasureSpec() {
    var spec = MeasureSpec.from(100.0, MeasureSpecMode.AtMost)
    assertThat(spec.size).isEqualTo(100.0)
    assertThat(spec.mode).isEqualTo(MeasureSpecMode.AtMost)

    spec = MeasureSpec.from(999.0, MeasureSpecMode.Exactly)
    assertThat(spec.size).isEqualTo(999.0)
    assertThat(spec.mode).isEqualTo(MeasureSpecMode.Exactly)

    spec = MeasureSpec.from(0.0, MeasureSpecMode.Unspecified)
    assertThat(spec.size).isEqualTo(0.0)
    assertThat(spec.mode).isEqualTo(MeasureSpecMode.Unspecified)
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
    val widthMeasureSpec = MeasureSpec.from(1000.0, MeasureSpecMode.Exactly)
    val heightMeasureSpec = MeasureSpec.from(500.0, MeasureSpecMode.Exactly)
    val lines = container.calculateFlexLines(widthMeasureSpec, heightMeasureSpec)
    assertThat(lines).hasSize(3)
    assertThat(lines[0].anyItemsHaveFlexGrow).isTrue()
    assertThat(lines[1].anyItemsHaveFlexGrow).isFalse()
    assertThat(lines[2].anyItemsHaveFlexGrow).isTrue()
  }

  class BoxMeasurable(
    override val requestedWidth: Double,
    override val requestedHeight: Double,
  ) : Measurable()
}
