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
 * Each child has initial intrinsic dimensions of 60 x 20.
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
 *
 * We confirm that we can handle resizes of the content by shrinking the content to 30 x 10 and
 * growing it to 90 x 30.
 */
class LayoutTester(
  subject: Subject,
  horizontalConstraint: Constraint,
  verticalConstraint: Constraint,
) {
  private val referenceView = RectangleUIView(60.0, 20.0)

  val top: UIView = RectangleUIView(60.0, 20.0)

  val middle: UIView = run {
    when (subject) {
      Subject.Reference -> referenceView

      Subject.TreehouseView -> TreehouseUIView(throwingWidgetSystem)
        .apply {
          (this.children as UIViewChildren).insert(0, viewWidget(referenceView))
        }
        .view
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
    screen.layoutIfNeeded()
    return middle.frameRectangle
  }

  fun shrinkSubject() {
    referenceView.width = 30.0
    referenceView.height = 10.0
  }

  fun growSubject() {
    referenceView.width = 90.0
    referenceView.height = 30.0
  }

  enum class Constraint(
    val initialX: Double,
    val initialY: Double,
    val initialWidth: Double,
    val initialHeight: Double,
    val shrunkX: Double = initialX,
    val shrunkY: Double = initialY,
    val shrunkWidth: Double = initialWidth,
    val shrunkHeight: Double = initialHeight,
    val grownX: Double = initialX,
    val grownY: Double = initialY,
    val grownWidth: Double = initialWidth,
    val grownHeight: Double = initialHeight,
  ) {
    Fill(
      // 120 x 40, centered at (60, 60)
      initialX = 0.0,
      initialY = 40.0,
      initialWidth = 120.0,
      initialHeight = 40.0,
    ),

    Center(
      // 60 x 20, centered at (60, 60)
      initialX = 30.0,
      initialY = 50.0,
      initialWidth = 60.0,
      initialHeight = 20.0,

      // 30 x 10, centered at (60, 60)
      shrunkX = 45.0,
      shrunkY = 55.0,
      shrunkWidth = 30.0,
      shrunkHeight = 10.0,

      // 90 x 30, centered at (60, 60)
      grownX = 15.0,
      grownY = 45.0,
      grownWidth = 90.0,
      grownHeight = 30.0,
    )
  }

  enum class Subject {
    Reference, TreehouseView
  }
}
