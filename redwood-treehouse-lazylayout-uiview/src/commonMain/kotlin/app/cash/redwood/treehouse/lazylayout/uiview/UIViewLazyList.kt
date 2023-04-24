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
import platform.Foundation.NSIndexPath
import platform.QuartzCore.CALayer
import platform.UIKit.UITableView
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellStyle
import platform.UIKit.UITableViewDiffableDataSource
import platform.UIKit.UIView
import platform.UIKit.row
import platform.UIKit.section
import platform.darwin.NSInteger

internal class UIViewLazyList<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: WidgetSystem,
) : LazyList<UIView> {
  private val dataSource = TableViewDataSource(treehouseApp, widgetSystem)
  private val root = UITableView().apply {
    this.dataSource = this@UIViewLazyList.dataSource
  }

  override fun isVertical(isVertical: Boolean) {
    if (!isVertical) {
      // TODO UITableView only supports vertical scrolling. Switch to UICollectionView.
      TODO()
    }
  }

  override fun intervals(intervals: List<LazyListInterval>) {
    dataSource.intervals = intervals
    root.reloadData()
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value: UIView get() = root
}

private class TableViewDataSource<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val widgetSystem: WidgetSystem,
) : UITableViewDiffableDataSource() {
  var intervals = emptyList<LazyListInterval>()

  override fun numberOfSectionsInTableView(tableView: UITableView): NSInteger {
    return intervals.size.toLong()
  }

  override fun tableView(tableView: UITableView, numberOfRowsInSection: NSInteger): NSInteger {
    return intervals[numberOfRowsInSection.toInt()].keys.size.toLong()
  }

  override fun tableView(tableView: UITableView, cellForRowAtIndexPath: NSIndexPath): UITableViewCell {
    val treehouseView = TreehouseUIKitView(widgetSystem)
    val cellContentSource = CellContentSource<A>(
      intervals[cellForRowAtIndexPath.section.toInt()].itemProvider,
      cellForRowAtIndexPath.row.toInt(),
    )
    cellContentSource.bindWhenReady(treehouseView, treehouseApp)
    return TableViewCell(treehouseView.view)
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

private class TableViewCell(private val view: UIView) :
  UITableViewCell(UITableViewCellStyle.UITableViewCellStyleDefault, null) {
  init {
    addSubview(view)
  }

  override fun layoutSublayersOfLayer(layer: CALayer) {
    view.setFrame(bounds)
  }
}
