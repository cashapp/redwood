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
package app.cash.redwood.treehouse

import app.cash.redwood.treehouse.TreehouseView.ReadyForContentChangeListener
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.widget.RedwoodUIView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.UIKit.UIScreen
import platform.UIKit.UITraitCollection
import platform.UIKit.UIView

@Deprecated(
  "Renamed to `TreehouseUIView` for consistency with other `TreehouseView` implementations.",
  ReplaceWith("TreehouseUIView", "app.cash.redwood.treehouse.TreehouseUIView"),
)
// `TreehouseUIKitView` should be the name in Objective C, but `@ObjCName` cannot be applied to a typealias.
public typealias TreehouseUIKitView = TreehouseUIView

@ObjCName("TreehouseUIView", exact = true)
public class TreehouseUIView private constructor(
  override val widgetSystem: WidgetSystem<UIView>,
  view: UIView,
) : TreehouseView<UIView>, RedwoodUIView(view) {
  override var saveCallback: TreehouseView.SaveCallback? = null
  override var stateSnapshotId: StateSnapshot.Id = StateSnapshot.Id(null)

  override var readyForContentChangeListener: ReadyForContentChangeListener<UIView>? = null
    set(value) {
      check(value == null || field == null) { "View already bound to a listener" }
      field = value
    }

  override val readyForContent: Boolean
    get() = view.superview != null

  public constructor(widgetSystem: WidgetSystem<UIView>) : this(widgetSystem, RootUiView())

  init {
    (view as RootUiView).treehouseView = this
  }

  private fun superviewChanged() {
    readyForContentChangeListener?.onReadyForContentChanged(this)
  }

  private class RootUiView : UIView(cValue { CGRectZero }) {
    lateinit var treehouseView: TreehouseUIView

    private var sizeThatFits = CGSizeMake(0.0, 0.0)

    /**
     * We've got a few things making layouts difficult:
     *
     *  * This view may be itself used with either Auto Layout or manual layout (like [sizeThatFits]
     *    and [setFrame]).
     *  * The contents are updated dynamically.
     *
     * It seems the simplest thing to do is to override [layoutSubviews], and to call
     * [invalidateIntrinsicContentSize] from within that if the frame we're given isn't the size
     * we want.
     *
     * https://mischa-hildebrand.de/en/2017/11/the-auto-layout-comprehendium/
     * https://forums.developer.apple.com/forums/thread/682973
     */
    override fun layoutSubviews() {
      super.layoutSubviews()

      val oldWidthThatFits = sizeThatFits.useContents { width }
      val oldHeightThatFits = sizeThatFits.useContents { height }

      // TODO(jessewilson): is passing the screen bounds to sizeThatFits() appropriate?
      this.sizeThatFits = sizeThatFits(
        UIScreen.mainScreen.bounds.useContents<CGRect, CValue<CGSize>> {
          CGSizeMake(size.width, size.height)
        },
      )
      val widthThatFits = sizeThatFits.useContents { width }
      val heightThatFits = sizeThatFits.useContents { height }

      // Trigger another layout if our intrinsic size was out of date.
      if (widthThatFits != oldWidthThatFits || heightThatFits != oldHeightThatFits) {
        invalidateIntrinsicContentSize()
      }

      val frameOrigin = frame.useContents { origin }
      val frameThatFits = CGRectMake(frameOrigin.x, frameOrigin.y, widthThatFits, heightThatFits)
      subviews.forEach {
        (it as UIView).setFrame(frameThatFits)
      }

      // Bounds likely changed. Report new size.
      treehouseView.updateUiConfiguration()
    }

    override fun intrinsicContentSize(): CValue<CGSize> = sizeThatFits

    override fun didAddSubview(subview: UIView) {
      super.didAddSubview(subview)
      invalidateIntrinsicContentSize()
    }

    override fun willRemoveSubview(subview: UIView) {
      super.willRemoveSubview(subview)
      invalidateIntrinsicContentSize()
    }

    override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
      var maxWidth = 0.0
      var maxHeight = 0.0
      subviews.forEach {
        val sizeThatFits = (it as UIView).sizeThatFits(size)
        maxWidth = maxOf(maxWidth, sizeThatFits.useContents { width })
        maxHeight = maxOf(maxHeight, sizeThatFits.useContents { height })
      }
      return CGSizeMake(maxWidth, maxHeight)
    }

    override fun didMoveToSuperview() {
      super.didMoveToSuperview()
      treehouseView.superviewChanged()
    }

    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
      super.traitCollectionDidChange(previousTraitCollection)
      treehouseView.updateUiConfiguration()
    }
  }
}
