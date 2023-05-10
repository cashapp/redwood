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
  "PARAMETER_NAME_CHANGED_ON_OVERRIDE",
  "RETURN_TYPE_MISMATCH_ON_OVERRIDE",
)

package app.cash.redwood.treehouse.lazylayout.uiview

import app.cash.redwood.LayoutModifier
import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.lazylayout.widget.LazyList
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.ObjCClass
import platform.Foundation.NSIndexPath
import platform.Foundation.classForCoder
import platform.UIKit.UITableView
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellStyle
import platform.UIKit.UITableViewCellStyle.UITableViewCellStyleDefault
import platform.UIKit.UITableViewDataSourceProtocol
import platform.UIKit.UITableViewDelegateProtocol
import platform.UIKit.UIView
import platform.UIKit.item
import platform.darwin.NSInteger
import platform.darwin.NSObject

private const val reuseIdentifier = "cell"

internal class UIViewLazyList<A : AppService>(
  treehouseApp: TreehouseApp<A>,
  widgetSystem: WidgetSystem,
) : LazyList<UIView> {

  private val itemsList = mutableListOf<Widget<UIView>>()

  override val items: Widget.Children<UIView> = object : Widget.Children<UIView> {
    override fun insert(index: Int, widget: Widget<UIView>) {
      itemsList.add(index, widget)
      tableView.reloadData()
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      TODO("Not yet implemented")
    }

    override fun remove(index: Int, count: Int) {
      for (i in 0 until count) {
        itemsList.removeAt(index)
      }
      tableView.reloadData()
    }

    override fun onLayoutModifierUpdated() {
    }
  }

  private val tableViewDelegate: UITableViewDelegateProtocol =
    object : NSObject(), UITableViewDelegateProtocol {
      override fun tableView(
        tableView: UITableView,
        willDisplayCell: UITableViewCell,
        forRowAtIndexPath: NSIndexPath,
      ) {
        val content = itemsList[forRowAtIndexPath.item.toInt()]
        (willDisplayCell as Cell).setWidget(content.value)
      }
    }

  private val tableViewDataSource: UITableViewDataSourceProtocol =
    object : NSObject(), UITableViewDataSourceProtocol {
      override fun numberOfSectionsInTableView(
        tableView: UITableView,
      ): NSInteger = 1L

      override fun tableView(
        tableView: UITableView,
        numberOfRowsInSection: NSInteger,
      ): NSInteger = itemsList.size.toLong()

      override fun tableView(
        tableView: UITableView,
        cellForRowAtIndexPath: NSIndexPath,
      ): UITableViewCell = tableView.dequeueReusableCellWithIdentifier(reuseIdentifier) as Cell
    }

  private val tableView = UITableView()
    .apply {
      dataSource = tableViewDataSource
      delegate = tableViewDelegate
      rowHeight = 64.0 // TODO: size rows by their content.
      prefetchingEnabled = false
      registerClass(
        Cell(UITableViewCellStyleDefault, reuseIdentifier).classForCoder() as ObjCClass?,
        forCellReuseIdentifier = reuseIdentifier,
      )
    }

  private val root = tableView

  private lateinit var onPositionDisplayed: (Int) -> Unit

  override fun isVertical(isVertical: Boolean) {
    if (!isVertical) {
      // TODO UITableView only supports vertical scrolling. Switch to UICollectionView.
      TODO()
    }
  }

  override fun onPositionDisplayed(onPositionDisplayed: (Int) -> Unit) {
    this.onPositionDisplayed = onPositionDisplayed
  }

  override var layoutModifiers: LayoutModifier = LayoutModifier

  override val value: UIView get() = root
}

private class Cell(
  style: UITableViewCellStyle,
  reuseIdentifier: String?,
) : UITableViewCell(style, reuseIdentifier) {
  private var widget: UIView? = null

  /** Factory function for a new cell. */
  override fun initWithStyle(
    style: UITableViewCellStyle,
    reuseIdentifier: String?,
  ): UITableViewCell = Cell(style, reuseIdentifier)

  override fun prepareForReuse() {
    super.prepareForReuse()
    this.widget?.removeFromSuperview()
  }

  fun setWidget(widget: UIView) {
    contentView.addSubview(widget)
    this.widget = widget
  }

  override fun layoutSubviews() {
    super.layoutSubviews()
    widget?.setFrame(bounds)
  }
}
