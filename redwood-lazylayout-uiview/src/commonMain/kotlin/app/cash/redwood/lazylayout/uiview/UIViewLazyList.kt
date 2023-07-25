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
@file:Suppress(
  "OVERRIDE_DEPRECATION",
  "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
  "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
)

package app.cash.redwood.lazylayout.uiview

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.lazylayout.widget.WindowedChildren
import app.cash.redwood.lazylayout.widget.WindowedLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlin.math.max
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSIndexPath
import platform.Foundation.classForCoder
import platform.UIKit.UICollectionView
import platform.UIKit.UICollectionViewCell
import platform.UIKit.UICollectionViewDataSourceProtocol
import platform.UIKit.UICollectionViewDelegateFlowLayoutProtocol
import platform.UIKit.UICollectionViewFlowLayout
import platform.UIKit.UICollectionViewLayout
import platform.UIKit.UICollectionViewScrollDirection
import platform.UIKit.UICollectionViewScrollPositionTop
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIRefreshControl
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.indexPathForItem
import platform.UIKit.item
import platform.darwin.NSInteger
import platform.darwin.NSObject

internal open class UIViewLazyList(
  private var collectionViewFlowLayout: UICollectionViewFlowLayout = UICollectionViewFlowLayout(),
  internal val collectionView: UICollectionView = UICollectionView(
    CGRectZero.readValue(),
    collectionViewFlowLayout,
  ),
) : WindowedLazyList<UIView>(UICollectionViewListUpdateCallback(collectionView)), ChangeListener {

  // Fetch the item relative to the entire collection view
  private fun WindowedChildren<UIView>.itemForGlobalIndex(index: Int): Widget<UIView> {
    return items.getOrNull(max(index - itemsBefore, 0))
      ?: placeholder[index % placeholder.size]
  }

  private val collectionViewDataSource: UICollectionViewDataSourceProtocol =
    object : NSObject(), UICollectionViewDataSourceProtocol {

      override fun collectionView(
        collectionView: UICollectionView,
        numberOfItemsInSection: NSInteger,
      ): NSInteger {
        return items.size.toLong()
      }

      override fun collectionView(
        collectionView: UICollectionView,
        cellForItemAtIndexPath: NSIndexPath,
      ): UICollectionViewCell {
        val cell = collectionView.dequeueReusableCellWithReuseIdentifier(
          identifier = reuseIdentifier,
          forIndexPath = cellForItemAtIndexPath,
        ) as LazyListContainerCell

        val widget = items.itemForGlobalIndex(cellForItemAtIndexPath.item.toInt())
        cell.set(widget.value)

        return cell
      }
    }

  private val collectionViewDelegate: UICollectionViewDelegateFlowLayoutProtocol =
    object : NSObject(), UICollectionViewDelegateFlowLayoutProtocol {
      override fun collectionView(
        collectionView: UICollectionView,
        layout: UICollectionViewLayout,
        sizeForItemAtIndexPath: NSIndexPath,
      ): CValue<CGSize> {
        val widget = items.itemForGlobalIndex(sizeForItemAtIndexPath.item.toInt())
        val itemSize = widget.value.sizeThatFits(collectionView.frame().useContents { size.readValue() })
        return if (isVertical) {
          CGSizeMake(collectionView.frame().useContents { size.width }, itemSize.useContents { height })
        } else {
          CGSizeMake(itemSize.useContents { width }, collectionView.frame().useContents { size.height })
        }
      }

      override fun collectionView(
        collectionView: UICollectionView,
        layout: UICollectionViewLayout,
        minimumLineSpacingForSectionAtIndex: NSInteger,
      ): CGFloat {
        return 0.0
      }

      override fun scrollViewDidScroll(scrollView: UIScrollView) {
        val visibleIndexPaths = collectionView.indexPathsForVisibleItems()

        if (visibleIndexPaths.isNotEmpty()) {
          // TODO: Optimize this for less operations
          updateViewport(
            visibleIndexPaths.minOf { (it as NSIndexPath).item.toInt() },
            visibleIndexPaths.maxOf { (it as NSIndexPath).item.toInt() },
          )
        }
      }
    }

  override val placeholder = MutableListChildren<UIView>()

  private var isVertical = true

  init {
    collectionView.apply {
      dataSource = collectionViewDataSource
      delegate = collectionViewDelegate
      prefetchingEnabled = true

      registerClass(
        LazyListContainerCell(CGRectZero.readValue()).classForCoder() as ObjCClass?,
        reuseIdentifier,
      )
    }
  }

  override fun isVertical(isVertical: Boolean) {
    this.isVertical = isVertical

    collectionViewFlowLayout.scrollDirection = if (isVertical) {
      UICollectionViewScrollDirection.UICollectionViewScrollDirectionVertical
    } else {
      UICollectionViewScrollDirection.UICollectionViewScrollDirectionHorizontal
    }
  }

  // TODO Dynamically update width and height of UIViewLazyList when set
  override fun width(width: Constraint) {}

  override fun height(height: Constraint) {}

  override fun margin(margin: Margin) {
    collectionView.contentInset = UIEdgeInsetsMake(margin.top.value, margin.start.value, margin.end.value, margin.bottom.value)
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    // TODO Support CrossAxisAlignment in `redwood-lazylayout-uiview`
  }

  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) {
    if (items.size > scrollItemIndex.index) {
      collectionView.scrollToItemAtIndexPath(
        NSIndexPath.indexPathForItem(scrollItemIndex.index.toLong(), 0),
        UICollectionViewScrollPositionTop,
        animated = false,
      )
    }
  }

  override var modifier: Modifier = Modifier

  override val value: UIView get() = collectionView
  override fun onEndChanges() {
    collectionView.reloadData()
  }
}

private const val reuseIdentifier = "LazyListContainerCell"

private class LazyListContainerCell(frame: CValue<CGRect>) : UICollectionViewCell(frame) {

  private var widgetView: UIView? = null
  override fun initWithFrame(frame: CValue<CGRect>): UICollectionViewCell = LazyListContainerCell(frame)
  override fun prepareForReuse() {
    super.prepareForReuse()
    // clear out subviews here
    this.contentView.subviews.forEach {
      (it as UIView).removeFromSuperview()
    }
    widgetView = null
  }

  fun set(widgetView: UIView) {
    this.widgetView = widgetView
    contentView.addSubview(widgetView)
    contentView.layoutIfNeeded()
  }

  override fun layoutSubviews() {
    super.layoutSubviews()
    widgetView?.setFrame(this.contentView.bounds)
  }
}

internal class UIViewRefreshableLazyList : UIViewLazyList(), RefreshableLazyList<UIView> {

  private var onRefresh: (() -> Unit)? = null

  private val refreshControl by lazy {
    UIRefreshControl().apply {
      setEventHandler(UIControlEventValueChanged) {
        onRefresh?.invoke()
      }
    }
  }

  override fun refreshing(refreshing: Boolean) {
    if (refreshing != refreshControl.refreshing) {
      if (refreshing) {
        refreshControl.beginRefreshing()
      } else {
        refreshControl.endRefreshing()
      }
    }
  }

  override fun onRefresh(onRefresh: (() -> Unit)?) {
    this.onRefresh = onRefresh

    if (onRefresh != null) {
      if (collectionView.refreshControl != refreshControl) {
        collectionView.refreshControl = refreshControl
      }
    } else {
      refreshControl.removeFromSuperview()
    }
  }

  override fun pullRefreshContentColor(pullRefreshContentColor: UInt) {
    refreshControl.tintColor = UIColor(pullRefreshContentColor)
  }
}

private fun UIColor(color: UInt): UIColor = UIColor(
  alpha = ((color and 0xFF000000u) shr 24).toDouble(),
  red = ((color and 0x00FF0000u) shr 16).toDouble(),
  green = ((color and 0x0000FF00u) shr 8).toDouble(),
  blue = (color and 0x000000FFu).toDouble(),
)
