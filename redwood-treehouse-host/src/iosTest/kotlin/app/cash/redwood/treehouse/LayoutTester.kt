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
package app.cash.redwood.treehouse

import app.cash.redwood.widget.UIViewChildren
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentCenter
import platform.UIKit.UIStackViewAlignmentFill
import platform.UIKit.UIStackViewDistributionEqualSpacing
import platform.UIKit.UIStackViewDistributionFillEqually
import platform.UIKit.UIView

/**
 * This layout puts 3 children in a 120 x 120 vertical stackview.
 *
 * Each child has an intrinsic width of 60px and an intrinsic height of 20px.
 *
 * ```
 * .-------------------------.
 * |                         |
 * |    .---------------.    |
 * |    |      top      |    |
 * |    '---------------'    |
 * |                         |
 * |    .---------------.    |
 * |    |    middle     |    |
 * |    '---------------'    |
 * |                         |
 * |    .---------------.    |
 * |    |    bottom     |    |
 * |    '---------------'    |
 * |                         |
 * '-------------------------'
 * ```
 *
 * Then we wrap the middle child in a TreehouseView and confirm the layouts all work as expected.
 * The presence of the TreehouseView wrapper shouldn't impact the layout.
 */
class LayoutTester(
  subject: Subject,
  horizontalConstraint: Constraint,
  verticalConstraint: Constraint,

  ) {
  val top: UIView = RectangleUIView(60.0, 20.0)

  val middle: UIView = run {
    val referenceView = RectangleUIView(60.0, 20.0)
    when (subject) {
      Subject.TreehouseView -> TreehouseUIView(throwingWidgetSystem)
        .apply {
          (this.children as UIViewChildren).insert(0, viewWidget(referenceView))
        }
        .view

      Subject.Reference -> referenceView
    }
  }

  val bottom: UIView = RectangleUIView(60.0, 20.0)

  val screen = UIStackView()
    .apply {
      axis = UILayoutConstraintAxisVertical
      alignment = when (horizontalConstraint) {
        Constraint.Fill -> UIStackViewAlignmentFill
        Constraint.Center -> UIStackViewAlignmentCenter
      }
      distribution = when (verticalConstraint) {
        Constraint.Fill -> UIStackViewDistributionFillEqually
        Constraint.Center -> UIStackViewDistributionEqualSpacing
      }

      setFrame(CGRectMake(0.0, 0.0, 120.0, 120.0))
      addArrangedSubview(top)
      addArrangedSubview(middle)
      addArrangedSubview(bottom)
    }

  fun subjectFrame(): Rectangle {
    println("------------------------------------------------")
    println("Before layout:")
    println("  screen=${screen.frameRectangle}")
    println("  a=${top.frameRectangle}")
    println("  b=${middle.frameRectangle}")
    println("  c=${bottom.frameRectangle}")

    screen.layoutIfNeeded()

    println("After layout:")
    println("  screen=${screen.frameRectangle}")
    println("  a=${top.frameRectangle}")
    println("  b=${middle.frameRectangle}")
    println("  c=${bottom.frameRectangle}")

    return middle.frameRectangle
  }

  enum class Constraint(
    val initialX: Double,
    val initialWidth: Double,
    val initialY: Double,
    val initialHeight: Double,
  ) {
    Fill(
      initialX = 0.0,
      initialWidth = 120.0,
      initialY = 40.0,
      initialHeight = 40.0,
    ),
    Center(
      initialX = 30.0,
      initialWidth = 60.0,
      initialY = 50.0,
      initialHeight = 20.0,
    )
  }

  enum class Subject {
    TreehouseView, Reference
  }
}
