/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package app.cash.redwood.layout.uiview

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.ui.Px
import app.cash.redwood.yoga.FlexDirection
import app.cash.redwood.yoga.Node
import app.cash.redwood.yoga.Size
import kotlin.math.max
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScrollView
import platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
import platform.UIKit.UIScrollViewDelegateProtocol
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric

internal class YogaUIView : UIScrollView(cValue { CGRectZero }), UIScrollViewDelegateProtocol {
  val rootNode = Node()
    .apply {
      this.context = this@YogaUIView
    }

  var widthConstraint = Constraint.Wrap
  var heightConstraint = Constraint.Wrap

  var onScroll: ((Px) -> Unit)? = null

  private var fillWidth = UIViewNoIntrinsicMetric
  private var fillHeight = UIViewNoIntrinsicMetric

  init {
    // TODO: Support scroll indicators.
    scrollEnabled = false
    showsVerticalScrollIndicator = false
    showsHorizontalScrollIndicator = false
    contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever
  }

  /**
   * The intrinsic size is broken by design if any subview's height depends on its width (or
   * vice-versa). For example, if a subview is UILabel that wraps, we need to know how wide the
   * label is before we can compute that label's height.
   *
   * We work around this by:
   *
   *  1. Making [intrinsicContentSize] depend on the mostly-recently applied bounds
   *  2. Invalidating it each time the bounds change
   *
   * This will result in an additional layout pass when the parent view uses [intrinsicContentSize].
   */
  override fun setBounds(bounds: CValue<CGRect>) {
    val newWidth = bounds.useContents { size.width }
    val newHeight = bounds.useContents { size.height }

    // Invalidate first because it clears fillWidth and fillHeight.
    if (
      (widthConstraint == Constraint.Fill && newWidth != fillWidth) ||
      (heightConstraint == Constraint.Fill && newHeight != fillHeight)
    ) {
      invalidateIntrinsicContentSize()
    }

    this.fillWidth = when (widthConstraint) {
      Constraint.Fill -> newWidth
      else -> UIViewNoIntrinsicMetric
    }
    this.fillHeight = when (heightConstraint) {
      Constraint.Fill -> newHeight
      else -> UIViewNoIntrinsicMetric
    }

    super.setBounds(bounds)
  }

  override fun invalidateIntrinsicContentSize() {
    super.invalidateIntrinsicContentSize()
    this.fillWidth = UIViewNoIntrinsicMetric
    this.fillHeight = UIViewNoIntrinsicMetric
  }

  override fun intrinsicContentSize(): CValue<CGSize> {
    return calculateLayout(
      width = fillWidth.toYogaWithWidthConstraint(),
      height = fillHeight.toYogaWithWidthConstraint(),
    )
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    return size.useContents<CGSize, CValue<CGSize>> {
      calculateLayout(
        width = width.toYogaWithWidthConstraint(),
        height = height.toYogaWithHeightConstraint(),
      )
    }
  }

  override fun layoutSubviews() {
    super.layoutSubviews()

    // Based on the constraints of Fill or Wrap, we
    // calculate a size that the container should fit in.
    val boundsSize = bounds.useContents {
      CGSizeMake(size.width, size.height)
    }

    val contentSize = when {
      // If we're not scrolling, the contentSize should equal the size of the view.
      !scrollEnabled -> boundsSize

      // When scrolling is enabled, we want to calculate and apply the contentSize
      // separately and have it grow a much as needed in the flexDirection.
      // This duplicates the calculation we're doing above, and should be
      // combined into one call.
      isColumn() -> calculateLayout(width = boundsSize.useContents { width.toYoga() })
      else -> calculateLayout(height = boundsSize.useContents { height.toYoga() })
    }

    setContentSize(contentSize)
    calculateLayout(
      width = contentSize.useContents { width.toYoga() },
      height = contentSize.useContents { height.toYoga() },
    )

    // Layout the nodes based on the calculatedLayouts above.
    for (childNode in rootNode.children) {
      (childNode.context as UIView).setFrame(
        CGRectMake(
          x = childNode.left.toDouble(),
          y = childNode.top.toDouble(),
          width = childNode.width.toDouble(),
          height = childNode.height.toDouble(),
        ),
      )
    }
  }

  private fun calculateLayout(
    width: Float = Size.UNDEFINED,
    height: Float = Size.UNDEFINED,
  ): CValue<CGSize> {
    rootNode.requestedWidth = width
    rootNode.requestedHeight = height

    rootNode.measureOnly(Size.UNDEFINED, Size.UNDEFINED)

    return CGSizeMake(rootNode.width.toDouble(), rootNode.height.toDouble())
  }

  private fun CGFloat.toYogaWithWidthConstraint() = when (widthConstraint) {
    Constraint.Wrap -> Size.UNDEFINED
    else -> toYoga()
  }

  private fun CGFloat.toYogaWithHeightConstraint() = when (heightConstraint) {
    Constraint.Wrap -> Size.UNDEFINED
    else -> toYoga()
  }

  /** Convert a UIView dimension (a Double) to a Yoga dimension (a Float). */
  private fun CGFloat.toYoga(): Float {
    return when (this) {
      UIViewNoIntrinsicMetric -> Size.UNDEFINED
      else -> this.toFloat()
    }
  }

  override fun setScrollEnabled(scrollEnabled: Boolean) {
    delegate = if (scrollEnabled) this else null

    val previousScrollEnabled = this.scrollEnabled

    super.setScrollEnabled(scrollEnabled)

    if (scrollEnabled != previousScrollEnabled) {
      setNeedsLayout()
    }
  }

  override fun scrollViewDidScroll(scrollView: UIScrollView) {
    val onScroll = onScroll
    if (onScroll != null) {
      val size = scrollView.bounds.useContents {
        if (isColumn()) size.height else size.width
      }
      val max = scrollView.contentSize.useContents {
        if (isColumn()) height else width
      }
      val offset = scrollView.contentOffset.useContents {
        if (isColumn()) y else x
      }.coerceIn(minimumValue = 0.0, maximumValue = max(0.0, max - size))
      onScroll(Px(offset))
    }
  }

  private fun isColumn(): Boolean {
    return rootNode.flexDirection == FlexDirection.Column
  }
}
