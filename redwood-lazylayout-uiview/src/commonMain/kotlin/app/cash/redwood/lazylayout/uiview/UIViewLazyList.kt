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
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlin.math.max
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
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
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UILabel
import platform.UIKit.UIRefreshControl
import platform.UIKit.UIScrollView
import platform.UIKit.UIView
import platform.UIKit.indexPathForRow
import platform.UIKit.item
import platform.darwin.NSInteger
import platform.darwin.NSObject

internal interface Items: Widget.Children<UIView> {
  var itemsBefore: Int
  var itemsAfter: Int
  var isFinishedInitialLoad: Boolean
  fun itemForGlobalIndex(index: Int): Widget<UIView>
  fun itemCount(): Int
}

internal open class UIViewLazyList() : LazyList<UIView> {

  // Data
  override val placeholder: Widget.Children<UIView> = MutableListChildren()

  override val items: Items = object : Items {

    override var itemsBefore: Int = 0
    override var itemsAfter: Int = 0
    override var isFinishedInitialLoad: Boolean = false

    private val viewPortList = mutableListOf<Widget<UIView>>()


    override fun insert(index: Int, widget: Widget<UIView>) {
      println("🟤 insert $index")
      viewPortList.add(index, widget)

      if (isFinishedInitialLoad) {
        val indexPath = globalIndexPathForViewportIndex(index)
       // collectionView.reloadItemsAtIndexPaths(indexPaths = listOf(indexPath))
      }

      // TODO: We need a signal that the items are done with their initial load..
      // This is because CollectionView and TableViews do not fetch their cells in ascending order
      // but does it randomly so we can't guarantee that we have enough items in here to fill the initial
      // set
      if (index == 13) {
        isFinishedInitialLoad = true
        collectionView.reloadData()
      }
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      println("🟤 move $fromIndex to $toIndex count: $count")
      viewPortList.move(fromIndex, toIndex, count)
      collectionView.reloadData()
    }

    override fun remove(index: Int, count: Int) {
      println("🟤 remove $index $count")
      viewPortList.remove(index, count)
    }

    override fun onModifierUpdated() {
      println("🟤 onModifierUpdated")
    }

    private fun globalIndexPathForViewportIndex(viewPortIndex: Int): NSIndexPath {
      val globalIndex = max(itemsBefore + (viewPortIndex - 1), 0)
      return NSIndexPath.indexPathForRow(globalIndex.toLong(), inSection = 0L)
    }

    // Fetch the item from the windowedList relative to the entire collection view
    override fun itemForGlobalIndex(index: Int): Widget<UIView> {
      val windowedIndex: Int = max(index - itemsBefore, 0)

      return if (windowedIndex < viewPortList.count()) {
        viewPortList[windowedIndex]
      } else {
        println("🔴 Error: index: $windowedIndex is outside of ${viewPortList.count()} item window size")

          // TODO: Replace with an error item
        viewPortList[0]
      }
    }

    override fun itemCount(): Int {
      return max(itemsBefore - 1, 0 ) + viewPortList.count() + itemsAfter
    }
  }

  private val viewPortListCoordinator = object {
    var minIndex: Int = 0
    var maxIndex: Int = 0

    fun notifyViewportChanged() {
      onViewportChanged(minIndex, maxIndex)
    }

    fun updateViewport(minIndex: Int, maxIndex: Int) {
      if (minIndex != this.minIndex || maxIndex != this.maxIndex) {
        this.minIndex = minIndex
        this.maxIndex = maxIndex

        println("🔵 viewport: [$minIndex $maxIndex]")
        notifyViewportChanged()
      }
    }
  }
  // A callback to tell LazyList that we have an update to the window of items available
  private lateinit var onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit

  // UICollectionView + Protocols

  // Must be initialized before the collectionView
  private var collectionViewFlowLayout: UICollectionViewFlowLayout =
    object: UICollectionViewFlowLayout() {}

  private val collectionViewDataSource: UICollectionViewDataSourceProtocol =
    object : NSObject(), UICollectionViewDataSourceProtocol {

      override fun collectionView(
        collectionView: UICollectionView,
        numberOfItemsInSection: NSInteger,
      ): NSInteger {
        return items.itemCount().toLong()
      }

      override fun collectionView(
        collectionView: UICollectionView,
        cellForItemAtIndexPath: NSIndexPath,
      ): UICollectionViewCell {
        val cell = collectionView.dequeueReusableCellWithReuseIdentifier(
          identifier = reuseIdentifier,
          forIndexPath = cellForItemAtIndexPath
        ) as LazyListContainerCell

        val widget = items.itemForGlobalIndex(cellForItemAtIndexPath.item.toInt())
        println("🟢 cellForItemAtIndexPath index: ${cellForItemAtIndexPath.item} count: ${items.itemCount()} text: ${(widget.value.subviews.first { it is UILabel } as UILabel).text}")
        cell.setView(widget.value)

        return cell
      }
    }

  private val collectionViewDelegate: UICollectionViewDelegateFlowLayoutProtocol =
    object: NSObject(), UICollectionViewDelegateFlowLayoutProtocol {
      override fun collectionView(
        collectionView: UICollectionView,
        layout: UICollectionViewLayout,
        sizeForItemAtIndexPath: NSIndexPath
      ): CValue<CGSize> {

        return CGSizeMake(collectionView.frame().useContents { size.width }, 80.0)

        // TODO: Size this dynamically based on the widget content
//        val item = items.itemForGlobalIndex(sizeForItemAtIndexPath.item.toInt())
//
//        if (collectionViewFlowLayout.scrollDirection == UICollectionViewScrollDirection.UICollectionViewScrollDirectionVertical) {
//          return CGSizeMake(collectionView.frame().useContents { size.width }, item.value.intrinsicContentSize.useContents { height })
//        } else {
//          return CGSizeMake(item.value.intrinsicContentSize.useContents { width }, collectionView.frame().useContents { size.height })
//        }
      }

      override fun scrollViewDidScroll(scrollView: UIScrollView) {
        val visibleIndexPaths = collectionView.indexPathsForVisibleItems()

        if (visibleIndexPaths.isNotEmpty()) {
          // TODO: Optimize this
          viewPortListCoordinator.updateViewport(
            visibleIndexPaths.minOf { (it as NSIndexPath).item.toInt() },
            visibleIndexPaths.maxOf { (it as NSIndexPath).item.toInt() }
          )
          if (!items.isFinishedInitialLoad) {
            items.isFinishedInitialLoad = true
            println("🟢 isFinishedInitialLoad scrollViewDidScroll")
          }
        }
      }
    }

  internal val collectionView = UICollectionView(
    frame = CGRectZero.readValue(),
    collectionViewLayout = this.collectionViewFlowLayout
  ).apply {
      dataSource = collectionViewDataSource
      delegate = collectionViewDelegate
      prefetchingEnabled = false

      registerClass(
        LazyListContainerCell(CGRectZero.readValue()).classForCoder() as ObjCClass?,
        reuseIdentifier
      )
    }

  // LazyList methods
  override fun isVertical(isVertical: Boolean) {
    println("🟤 isVertical")
    if (!isVertical) {
      collectionViewFlowLayout.scrollDirection = if (isVertical) {
        UICollectionViewScrollDirection.UICollectionViewScrollDirectionVertical
      } else {
        UICollectionViewScrollDirection.UICollectionViewScrollDirectionHorizontal
      }
    }
  }

  override fun onViewportChanged(onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit) {
    println("🟤 onViewportChanged")
    this.onViewportChanged = onViewportChanged
  }

  override fun itemsBefore(itemsBefore: Int) {
    println("🟤 itemsBefore $itemsBefore")
    items.itemsBefore = itemsBefore
  }

  override fun itemsAfter(itemsAfter: Int) {
    println("🟤 itemsAfter $itemsAfter")
    items.itemsAfter = itemsAfter
  }

  // TODO Dynamically update width and height of UIViewLazyList when set
  // @Veyndan - Shouldn't this be done by the parent?
  override fun width(width: Constraint) {
    println("🟤 width")
  }

  override fun height(height: Constraint) {
    println("🟤 height")
  }

  override fun margin(margin: Margin) {
    println("🟤 margin")
    collectionView.contentInset = UIEdgeInsetsMake(margin.top.value, margin.start.value, margin.end.value, margin.bottom.value)
  }

  override var modifier: Modifier = Modifier

  override val value: UIView get() = collectionView
}

private const val reuseIdentifier = "LazyListContainerCell"
private class LazyListContainerCell(frame: CValue<CGRect>) : UICollectionViewCell(frame) {

  private var view: UIView = UIView()
  override fun initWithFrame(frame: CValue<CGRect>): UICollectionViewCell = LazyListContainerCell(frame)
  fun setView(view: UIView) {
    if (this.view != view) {
      this.view.removeFromSuperview()
      this.view = view
      contentView.addSubview(view)
    }
  }
  override fun layoutSubviews() {
    super.layoutSubviews()
    view.setFrame(this.contentView.bounds)
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
}
