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
package app.cash.redwood.treehouse.lazylayout.uiview

import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseContentSource
import app.cash.redwood.treehouse.TreehouseUIKitView
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.bindWhenReady
import app.cash.redwood.treehouse.lazylayout.api.LazyListInterval
import app.cash.redwood.treehouse.lazylayout.widget.LazyList
import kotlinx.cinterop.CValue
import kotlinx.cinterop.cValue
import kotlinx.cinterop.copy
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGSize
import platform.Foundation.NSIndexPath
import platform.QuartzCore.CALayer
import platform.UIKit.UICollectionView
import platform.UIKit.UICollectionViewCell
import platform.UIKit.UICollectionViewCellMeta
import platform.UIKit.UICollectionViewController
import platform.UIKit.UICollectionViewDelegateFlowLayoutProtocol
import platform.UIKit.UICollectionViewDiffableDataSource
import platform.UIKit.UICollectionViewFlowLayout
import platform.UIKit.UICollectionViewLayout
import platform.UIKit.UICollectionViewScrollDirection.UICollectionViewScrollDirectionHorizontal
import platform.UIKit.UICollectionViewScrollDirection.UICollectionViewScrollDirectionVertical
import platform.UIKit.UIView
import platform.UIKit.row
import platform.UIKit.section
import platform.darwin.NSInteger

internal class UIViewLazyList<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: WidgetSystem,
) : LazyList<UIView> {
  private val layout = UICollectionViewFlowLayout()
  private val viewController = CollectionViewController(treehouseApp, widgetSystem, layout)

  override fun isVertical(isVertical: Boolean) {
    layout.scrollDirection = if (isVertical) {
      UICollectionViewScrollDirectionVertical
    } else {
      UICollectionViewScrollDirectionHorizontal
    }
  }

  override fun intervals(intervals: List<LazyListInterval>) {
    viewController.dataSource.intervals = intervals
    viewController.collectionView.reloadData()
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value: UIView get() = viewController.collectionView
}

private class CollectionViewController<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: WidgetSystem,
  layout: UICollectionViewLayout,
) : UICollectionViewController(layout),
  UICollectionViewDelegateFlowLayoutProtocol {
  val dataSource = CollectionViewDataSource(treehouseApp, widgetSystem)

  init {
    collectionView.registerClass(CollectionViewCell, "CollectionViewCell")
    collectionView.dataSource = dataSource
  }

  override fun collectionView(collectionView: UICollectionView, layout: UICollectionViewLayout, sizeForItemAtIndexPath: NSIndexPath): CValue<CGSize> {
    // TODO Flip the dimensions when isVertical==false.
    return collectionView.sizeThatFits(cValue { collectionView.bounds.useContents { size } }).copy {
      height = 64.0
    }
  }
}

private class CollectionViewDataSource<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val widgetSystem: WidgetSystem,
) : UICollectionViewDiffableDataSource() {
  var intervals = emptyList<LazyListInterval>()

  override fun numberOfSectionsInCollectionView(collectionView: UICollectionView): NSInteger {
    return intervals.size.toLong()
  }

  override fun collectionView(collectionView: UICollectionView, numberOfItemsInSection: NSInteger): NSInteger {
    return intervals[numberOfItemsInSection.toInt()].keys.size.toLong()
  }

  override fun collectionView(collectionView: UICollectionView, cellForItemAtIndexPath: NSIndexPath): UICollectionViewCell {
    val collectionViewCell = collectionView.dequeueReusableCellWithReuseIdentifier("CollectionViewCell", cellForItemAtIndexPath) as CollectionViewCell
    val treehouseView = TreehouseUIKitView(widgetSystem)
    collectionViewCell.view = treehouseView.view
    val cellContentSource = CellContentSource<A>(
      intervals[cellForItemAtIndexPath.section.toInt()].itemProvider,
      cellForItemAtIndexPath.row.toInt(),
    )
    cellContentSource.bindWhenReady(treehouseView, treehouseApp)
    collectionViewCell.subviews.forEach { (it as UIView).removeFromSuperview() }
    collectionViewCell.addSubview(treehouseView.view)
    return collectionViewCell
  }
}

private class CellContentSource<A : AppService>(
  private val itemProvider: LazyListInterval.Item,
  private val index: Int,
) : TreehouseContentSource<A> {

  override fun get(app: A): ZiplineTreehouseUi {
    return itemProvider.get(index)
  }
}

private class CollectionViewCell(frame: CValue<CGRect>) : UICollectionViewCell(frame) {
  var view: UIView? = null

  override fun layoutSublayersOfLayer(layer: CALayer) {
    view!!.setFrame(bounds)
  }

  // If this isn't included, an 'Initializer is not implemented' exception is thrown.
  @Suppress("OVERRIDE_DEPRECATION")
  override fun initWithFrame(frame: CValue<CGRect>): UICollectionViewCell {
    return CollectionViewCell(frame)
  }

  companion object : UICollectionViewCellMeta()
}
