/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.layout

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.layout.widget.Box
import app.cash.redwood.ui.Dp
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.dp
import kotlin.test.Test

abstract class AbstractBoxTest<T : Any> {

  abstract fun box(): Box<T>

  abstract fun color(): Color<T>

  abstract fun text(): Text<T>

  fun color(color: Int, width: Dp, height: Dp) = color().apply {
    color(color)
    width(width)
    height(height)
  }

  abstract fun verifySnapshot(widget: T, name: String? = null)

  @Test
  fun testEmpty_Defaults() {
    val widget = box()
    verifySnapshot(widget.value)
  }

  @Test
  fun testEmpty_Wrap() {
    val widget = box().apply {
      width(Constraint.Wrap)
      height(Constraint.Wrap)
    }
    verifySnapshot(widget.value)
  }

  @Test
  fun testEmpty_Fill() {
    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
    }
    verifySnapshot(widget.value)
  }

  // testChildren

  @Test
  fun testChildren_Wrap_Start_Start() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Wrap_Center_Start() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Wrap_End_Start() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Wrap_Stretch_Start() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Wrap_Start_Center() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Wrap_Center_Center() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Wrap_End_Center() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Wrap_Stretch_Center() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Wrap_Start_End() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Wrap_Center_End() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Wrap_End_End() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Wrap_Stretch_End() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Wrap_Start_Stretch() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Wrap_Center_Stretch() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Wrap_End_Stretch() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Wrap_Stretch_Stretch() = testChildren(
    constraint = Constraint.Wrap,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Fill_Start_Start() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Fill_Center_Start() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Fill_End_Start() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Fill_Stretch_Start() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.Start,
  )

  @Test
  fun testChildren_Fill_Start_Center() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Fill_Center_Center() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Fill_End_Center() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Fill_Stretch_Center() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.Center,
  )

  @Test
  fun testChildren_Fill_Start_End() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Fill_Center_End() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Fill_End_End() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Fill_Stretch_End() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.End,
  )

  @Test
  fun testChildren_Fill_Start_Stretch() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Start,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Fill_Center_Stretch() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Center,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Fill_End_Stretch() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.End,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  @Test
  fun testChildren_Fill_Stretch_Stretch() = testChildren(
    constraint = Constraint.Fill,
    horizontalAlignment = CrossAxisAlignment.Stretch,
    verticalAlignment = CrossAxisAlignment.Stretch,
  )

  private fun testChildren(
    constraint: Constraint,
    horizontalAlignment: CrossAxisAlignment,
    verticalAlignment: CrossAxisAlignment,
  ) {
    val widget = box().apply {
      width(constraint)
      height(constraint)
      horizontalAlignment(horizontalAlignment)
      verticalAlignment(verticalAlignment)
      children.insert(
        0,
        text().apply {
          text(longText())
          bgColor(Red)
        },
      )
      children.insert(
        1,
        text().apply {
          text(mediumText())
          bgColor(Green)
        },
      )
      children.insert(
        2,
        text().apply {
          text(shortText())
          bgColor(Blue)
        },
      )
    }
    verifySnapshot(widget.value)
  }

  @Test
  fun testMargins() {
    // Different margins allow us to know which direction start and end get applied.
    val asymmetric = Margin(start = 10.dp, top = 20.dp, end = 30.dp, bottom = 40.dp)

    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)

      // Ensure Box applies its margins correctly to the parent.
      margin(asymmetric)

      children.insert(
        0,
        color(Red, 100.dp, 100.dp).apply {
          // Ensure Box honors margins correctly from its children.
          modifier = MarginImpl(asymmetric)
        },
      )
    }
    verifySnapshot(widget.value)
  }

  @Test
  fun testChildrenModifierChanges() {
    val redColor = coloredText(MarginImpl(30.dp), longText(), Red)
    val widget = box().apply {
      width(Constraint.Fill)
      height(Constraint.Fill)
      children.insert(0, redColor)
      children.insert(1, coloredText(text = mediumText(), color = Blue))
      children.insert(2, coloredText(text = shortText(), color = Green))
    }
    verifySnapshot(widget.value, "Margin")
    redColor.modifier = Modifier
    widget.children.onModifierUpdated(0, redColor)
    verifySnapshot(widget.value, "Empty")
  }

  /** The view shouldn't crash if its displayed after being detached. */
  @Test
  fun testLayoutAfterDetach() {
    val widget = box().apply {
      width(Constraint.Wrap)
      height(Constraint.Wrap)
      horizontalAlignment(CrossAxisAlignment.Start)
      verticalAlignment(CrossAxisAlignment.Start)
    }

    // Render before calling detach().
    widget.children.insert(0, coloredText(MarginImpl(10.dp), mediumText(), Green))
    widget.children.insert(1, coloredText(MarginImpl(0.dp), shortText(), Blue))
    verifySnapshot(widget.value, "Before")

    // Detach after changes are applied but before they're rendered.
    widget.children.insert(0, coloredText(MarginImpl(20.dp), longText(), Red))
    widget.children.detach()
    verifySnapshot(widget.value, "After")
  }

  @Test fun testDynamicWidgetResizing() {
    val container = box()
      .apply {
        width(Constraint.Fill)
        height(Constraint.Fill)
        horizontalAlignment(CrossAxisAlignment.Start)
        verticalAlignment(CrossAxisAlignment.Start)
      }

    val a = coloredText(text = "AAA", color = Red)
      .apply { modifier = HorizontalAlignmentImpl(CrossAxisAlignment.Start) }
      .also { container.children.insert(0, it) }
    val b = coloredText(text = "BBB", color = Blue)
      .apply { modifier = HorizontalAlignmentImpl(CrossAxisAlignment.Center) }
      .also { container.children.insert(1, it) }
    val c = coloredText(text = "CCC", color = Green)
      .apply { modifier = HorizontalAlignmentImpl(CrossAxisAlignment.End) }
      .also { container.children.insert(2, it) }
    verifySnapshot(container.value, "v1")

    b.text("BBB_v2")
    verifySnapshot(container.value, "v2")
  }

  private fun coloredText(modifier: Modifier = Modifier, text: String, color: Int) = text().apply {
    text(text)
    bgColor(color)
    this.modifier = modifier
  }
}
