/*
 * Copyright (C) 2023 Square, Inc.
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
package app.cash.redwood.widget

import app.cash.redwood.ui.Cancellable
import app.cash.redwood.ui.Default
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.LayoutDirection
import app.cash.redwood.ui.Margin
import app.cash.redwood.ui.OnBackPressedCallback
import app.cash.redwood.ui.OnBackPressedDispatcher
import app.cash.redwood.ui.Size
import app.cash.redwood.ui.UiConfiguration
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.convert
import kotlinx.cinterop.useContents
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIApplication
import platform.UIKit.UIEdgeInsets
import platform.UIKit.UIEdgeInsetsZero
import platform.UIKit.UILayoutPriority
import platform.UIKit.UITraitCollection
import platform.UIKit.UIUserInterfaceLayoutDirection
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionLeftToRight
import platform.UIKit.UIUserInterfaceLayoutDirection.UIUserInterfaceLayoutDirectionRightToLeft
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIView
import platform.UIKit.UIViewNoIntrinsicMetric
import platform.darwin.NSInteger

/**
 * This view must be explicitly configured to know whether its dimensions are derived by measuring
 * its contents, or whether they are provided by the superview. Use [fillWidth] and [fillHeight] to
 * accept the parent's provided width and height respectively.
 */
@ObjCName("RedwoodUIView", exact = true)
public open class RedwoodUIView : RedwoodView<UIView> {
  private val valueRootView: RootUIStackView = RootUIStackView()

  override val value: UIView
    get() = valueRootView

  private val sizeListener = object : ResizableWidget.SizeListener {
    override fun invalidateSize() {
      // This view's size may have changed.
      valueRootView.setNeedsLayout() // For autolayout.
      valueRootView.invalidateIntrinsicContentSize() // For SwiftUI.

      // And the superview should redo its layout also, if it exists.
      valueRootView.superview?.setNeedsLayout() // For autolayout.
      valueRootView.superview?.invalidateIntrinsicContentSize() // For SwiftUI.
    }
  }

  private val _children = UIViewChildren(
    container = valueRootView,
    insert = { index, widget ->
      if (widget is ResizableWidget<*>) {
        widget.sizeListener = sizeListener
      }
      valueRootView.insertSubview(widget.value, index.convert<NSInteger>())
    },
    invalidateSize = sizeListener::invalidateSize,
  )

  override val children: Widget.Children<UIView>
    get() = _children

  private val density: Density get() = Density.Default

  private val mutableUiConfiguration =
    MutableStateFlow(
      computeUiConfiguration(
        density = density,
        traitCollection = valueRootView.traitCollection,
        viewInsets = valueRootView.incomingSafeAreaInsets.toMargin(),
        layoutDirection = valueRootView.effectiveUserInterfaceLayoutDirection,
        bounds = valueRootView.bounds,
      ),
    )

  override val onBackPressedDispatcher: OnBackPressedDispatcher = object : OnBackPressedDispatcher {
    override fun addCallback(onBackPressedCallback: OnBackPressedCallback): Cancellable {
      return object : Cancellable {
        override fun cancel() = Unit
      }
    }
  }

  override val uiConfiguration: StateFlow<UiConfiguration>
    get() = mutableUiConfiguration

  override val savedStateRegistry: SavedStateRegistry?
    get() = null

  public var fillWidth: Boolean = false
  public var fillHeight: Boolean = false

  private fun updateUiConfiguration() {
    mutableUiConfiguration.value = computeUiConfiguration(
      density = density,
      traitCollection = valueRootView.traitCollection,
      viewInsets = valueRootView.incomingSafeAreaInsets.toMargin(),
      layoutDirection = valueRootView.effectiveUserInterfaceLayoutDirection,
      bounds = valueRootView.bounds,
    )
  }

  protected open fun superviewChanged() {
  }

  /**
   * In practice we expect this to contain either zero child subviews (especially when
   * newly-initialized) or one child subview, which will usually be a layout container.
   *
   * This is a custom layout to best support callers using either SwiftUI (which calls through
   * [sizeThatFits]) or autolayout (which calls through [intrinsicContentSize]).
   */
  private inner class RootUIStackView : UIView(cValue { CGRectZero }) {
    /** Safe area insets specified by the superview. */
    val incomingSafeAreaInsets: CValue<UIEdgeInsets>
      get() = super.safeAreaInsets

    private var widthForIntrinsicSize = UIViewNoIntrinsicMetric
      set(value) {
        field = value
        invalidateIntrinsicContentSize()
      }
    private var heightForIntrinsicSize = UIViewNoIntrinsicMetric
      set(value) {
        field = value
        invalidateIntrinsicContentSize()
      }

    init {
      this.setInsetsLayoutMarginsFromSafeArea(false) // Consume insets internally.
    }

    /**
     * Safe area insets propagated to subviews is always zero. They consume insets from
     * [UiConfiguration], and this override prevents doubling insets.
     */
    override fun safeAreaInsets() = cValue<UIEdgeInsets> { UIEdgeInsetsZero }

    override fun safeAreaInsetsDidChange() {
      super.safeAreaInsetsDidChange()
      updateUiConfiguration()
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      return maxSizeOfSubviews { it.sizeThatFits(size) }
    }

    override fun systemLayoutSizeFittingSize(targetSize: CValue<CGSize>): CValue<CGSize> {
      return maxSizeOfSubviews { it.systemLayoutSizeFittingSize(targetSize) }
    }

    override fun systemLayoutSizeFittingSize(
      targetSize: CValue<CGSize>,
      withHorizontalFittingPriority: UILayoutPriority,
      verticalFittingPriority: UILayoutPriority,
    ): CValue<CGSize> {
      return maxSizeOfSubviews {
        it.systemLayoutSizeFittingSize(
          targetSize,
          withHorizontalFittingPriority,
          verticalFittingPriority,
        )
      }
    }

    override fun setBounds(bounds: CValue<CGRect>) {
      updateFrameForIntrinsicSize(bounds)
      super.setBounds(bounds)
    }

    override fun setFrame(frame: CValue<CGRect>) {
      updateFrameForIntrinsicSize(frame)
      super.setFrame(frame)
    }

    /**
     * The intrinsic size is broken by design if any subview's height depends on its width (or
     * vice-versa). For example, if a subview is UILabel that wraps, we need to know how wide the
     * label is before we can compute that label's height.
     *
     * We work around this by:
     *
     *  1. Making [intrinsicContentSize] depend on the mostly-recently applied frame
     *  2. Invalidating it each time the frame changes
     *
     * This will result in an additional layout pass when the superview uses [intrinsicContentSize].
     */
    private fun updateFrameForIntrinsicSize(frame: CValue<CGRect>) {
      val newWidth = frame.useContents { size.width }
      val newHeight = frame.useContents { size.height }

      if (
        (fillWidth && newWidth != widthForIntrinsicSize) ||
        (fillHeight && newHeight != heightForIntrinsicSize)
      ) {
        invalidateIntrinsicContentSize()
      }

      this.widthForIntrinsicSize = when {
        fillWidth -> newWidth
        else -> UIViewNoIntrinsicMetric
      }
      this.heightForIntrinsicSize = when {
        fillHeight -> newHeight
        else -> UIViewNoIntrinsicMetric
      }
    }

    override fun intrinsicContentSize(): CValue<CGSize> {
      return when {
        widthForIntrinsicSize == UIViewNoIntrinsicMetric &&
          heightForIntrinsicSize == UIViewNoIntrinsicMetric -> {
          maxSizeOfSubviews { it.intrinsicContentSize() }
        }

        else -> {
          sizeThatFits(CGSizeMake(widthForIntrinsicSize, heightForIntrinsicSize))
        }
      }
    }

    private fun maxSizeOfSubviews(
      measure: (UIView) -> CValue<CGSize>,
    ): CValue<CGSize> {
      var maxWidth = 0.0
      var maxHeight = 0.0

      for (subview in subviews) {
        val subviewSize = measure(subview as UIView)
        subviewSize.useContents {
          maxWidth = maxOf(width, maxWidth)
          maxHeight = maxOf(height, maxHeight)
        }
      }

      return CGSizeMake(maxWidth, maxHeight)
    }

    override fun layoutSubviews() {
      val width = frame.useContents { size.width }
      val height = frame.useContents { size.height }

      for (subview in subviews) {
        (subview as UIView).setFrame(CGRectMake(0.0, 0.0, width, height))
      }

      super.layoutSubviews()

      // Bounds likely changed. Report new size.
      updateUiConfiguration()
    }

    override fun didMoveToSuperview() {
      super.didMoveToSuperview()
      superviewChanged()
    }

    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
      super.traitCollectionDidChange(previousTraitCollection)
      updateUiConfiguration()
    }
  }

  private fun CValue<UIEdgeInsets>.toMargin(): Margin {
    return with(density) {
      useContents {
        Margin(left.toDp(), right.toDp(), top.toDp(), bottom.toDp())
      }
    }
  }
}

internal fun computeUiConfiguration(
  density: Density,
  traitCollection: UITraitCollection,
  viewInsets: Margin,
  layoutDirection: UIUserInterfaceLayoutDirection,
  bounds: CValue<CGRect>,
): UiConfiguration {
  return UiConfiguration(
    darkMode = traitCollection.userInterfaceStyle == UIUserInterfaceStyle.UIUserInterfaceStyleDark,
    safeAreaInsets = computeSafeAreaInsets(),
    viewInsets = viewInsets,
    viewportSize = bounds.useContents {
      with(density) {
        Size(size.width.toDp(), size.height.toDp())
      }
    },
    density = density.rawDensity,
    layoutDirection = when (layoutDirection) {
      UIUserInterfaceLayoutDirectionRightToLeft -> LayoutDirection.Rtl
      UIUserInterfaceLayoutDirectionLeftToRight -> LayoutDirection.Ltr
      else -> throw IllegalArgumentException("Layout direction must be RightToLeft or LeftToRight")
    },
  )
}

private fun computeSafeAreaInsets(): Margin {
  val keyWindow = UIApplication.sharedApplication.keyWindow ?: return Margin.Zero
  return keyWindow.safeAreaInsets.useContents {
    with(Density.Default) {
      Margin(left.toDp(), right.toDp(), top.toDp(), bottom.toDp())
    }
  }
}
