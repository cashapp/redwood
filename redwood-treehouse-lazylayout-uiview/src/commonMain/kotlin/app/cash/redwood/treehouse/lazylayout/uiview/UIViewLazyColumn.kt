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
import app.cash.redwood.treehouse.TreehouseUIKitView
import app.cash.redwood.treehouse.TreehouseView
import app.cash.redwood.treehouse.ZiplineTreehouseUi
import app.cash.redwood.treehouse.lazylayout.api.LazyListIntervalContent
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import platform.Foundation.NSIndexPath
import platform.QuartzCore.CALayer
import platform.UIKit.UITableView
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellStyle
import platform.UIKit.UITableViewDiffableDataSource
import platform.UIKit.UIView
import platform.UIKit.addSubview
import platform.UIKit.row
import platform.UIKit.section
import platform.UIKit.setFrame
import platform.darwin.NSInteger

internal class UIViewLazyColumn<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: TreehouseView.WidgetSystem<A>,
) : LazyColumn<UIView> {
  private val dataSource = TableViewDataSource(treehouseApp, widgetSystem)
  private val root = UITableView().apply {
    this.dataSource = this@UIViewLazyColumn.dataSource
  }

  override fun intervals(intervals: List<LazyListIntervalContent>) {
    dataSource.intervals = intervals
    root.reloadData()
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value: UIView get() = root
}

private class TableViewDataSource<A : AppService>(
  private val treehouseApp: TreehouseApp<A>,
  private val widgetSystem: TreehouseView.WidgetSystem<A>,
) : UITableViewDiffableDataSource() {
  var intervals = emptyList<LazyListIntervalContent>()

  override fun numberOfSectionsInTableView(tableView: UITableView): NSInteger {
    return intervals.size.toLong()
  }

  override fun tableView(tableView: UITableView, numberOfRowsInSection: NSInteger): NSInteger {
    return intervals[numberOfRowsInSection.toInt()].count.toLong()
  }

  override fun tableView(tableView: UITableView, cellForRowAtIndexPath: NSIndexPath): UITableViewCell {
    val treehouseView = TreehouseUIKitView(widgetSystem)
    treehouseApp.renderTo(treehouseView)
    treehouseView.setContent(CellContent(intervals[cellForRowAtIndexPath.section.toInt()].itemProvider, cellForRowAtIndexPath.row.toInt()))
    return TableViewCell(treehouseView.view)
  }
}

private class CellContent<A : AppService>(
  private val itemProvider: LazyListIntervalContent.Item,
  private val index: Int,
) : TreehouseView.Content<A> {

  override fun get(app: A): ZiplineTreehouseUi {
    return itemProvider.get(index)
  }
}

private class TableViewCell(private val view: UIView) : UITableViewCell(UITableViewCellStyle.UITableViewCellStyleDefault, null) {
  init {
    addSubview(view)
  }

  override fun layoutSublayersOfLayer(layer: CALayer) {
    view.setFrame(bounds)
  }
}
