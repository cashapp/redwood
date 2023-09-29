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
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.LazyListUpdateProcessor
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSize
import platform.Foundation.NSIndexPath
import platform.Foundation.classForCoder
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIRefreshControl
import platform.UIKit.UIScrollView
import platform.UIKit.UITableView
import platform.UIKit.UITableViewAutomaticDimension
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellSeparatorStyle.UITableViewCellSeparatorStyleNone
import platform.UIKit.UITableViewCellStyle
import platform.UIKit.UITableViewDataSourceProtocol
import platform.UIKit.UITableViewDelegateProtocol
import platform.UIKit.UITableViewRowAnimationNone
import platform.UIKit.UITableViewScrollPosition
import platform.UIKit.UIView
import platform.UIKit.indexPathForItem
import platform.UIKit.item
import platform.darwin.NSInteger
import platform.darwin.NSObject

internal open class UIViewLazyList(
  internal val tableView: UITableView = UITableView(
    CGRectZero.readValue(),
  ),
) : LazyList<UIView>, ChangeListener {
  override var modifier: Modifier = Modifier

  override val value: UIView
    get() = tableView

  protected var onViewportChanged: ((firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit)? = null

  private val processor = object : LazyListUpdateProcessor<LazyListContainerCell, UIView>() {
    override fun createCell(
      cell: Cell<LazyListContainerCell, UIView>,
      widget: Widget<UIView>,
      index: Int,
    ): LazyListContainerCell {
      val result = tableView.dequeueReusableCellWithIdentifier(
        identifier = reuseIdentifier,
        forIndexPath = NSIndexPath.indexPathForItem(index.convert(), 0.convert()),
      ) as LazyListContainerCell
      require(result.cell == null)
      result.cell = cell
      result.setWidget(widget.value)
      return result
    }

    override fun setWidget(cell: LazyListContainerCell, widget: Widget<UIView>) {
      cell.setWidget(widget.value)
    }

    override fun insertRows(index: Int, count: Int) {
      // TODO(jwilson): pass a range somehow when 'count' is large?
      tableView.insertRowsAtIndexPaths(
        (index until index + count).map { NSIndexPath.indexPathForItem(it.convert(), 0) },
        UITableViewRowAnimationNone,
      )
    }

    override fun deleteRows(index: Int, count: Int) {
      // TODO(jwilson): pass a range somehow when 'count' is large?
      tableView.deleteRowsAtIndexPaths(
        (index until index + count).map { NSIndexPath.indexPathForItem(it.convert(), 0) },
        UITableViewRowAnimationNone,
      )
    }
  }

  override val placeholder: Widget.Children<UIView> = processor.placeholder

  override val items: Widget.Children<UIView> = processor.items

  private val dataSource = object : NSObject(), UITableViewDataSourceProtocol {
    override fun tableView(
      tableView: UITableView,
      numberOfRowsInSection: NSInteger,
    ) = processor.size.toLong()

    override fun tableView(
      tableView: UITableView,
      cellForRowAtIndexPath: NSIndexPath,
    ) = processor.getCell(cellForRowAtIndexPath.item.toInt())
  }

  private val tableViewDelegate: UITableViewDelegateProtocol =
    object : NSObject(), UITableViewDelegateProtocol {
      override fun scrollViewDidScroll(scrollView: UIScrollView) {
        val visibleIndexPaths = tableView.indexPathsForVisibleRows ?: return

        if (visibleIndexPaths.isNotEmpty()) {
          // TODO: Optimize this for less operations
          onViewportChanged?.invoke(
            visibleIndexPaths.minOf { (it as NSIndexPath).item.toInt() },
            visibleIndexPaths.maxOf { (it as NSIndexPath).item.toInt() },
          )
        }
      }
    }

  init {
    tableView.apply {
      dataSource = this@UIViewLazyList.dataSource
      delegate = tableViewDelegate
      prefetchingEnabled = true
      rowHeight = UITableViewAutomaticDimension
      estimatedRowHeight = 44.0
      separatorStyle = UITableViewCellSeparatorStyleNone

      registerClass(
        cellClass = LazyListContainerCell(UITableViewCellStyle.UITableViewCellStyleDefault, reuseIdentifier)
          .initWithFrame(CGRectZero.readValue()).classForCoder() as ObjCClass?,
        forCellReuseIdentifier = reuseIdentifier,
      )
    }
  }

  final override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) {
    this.onViewportChanged = onViewportChanged
  }

  override fun isVertical(isVertical: Boolean) {
    // TODO: support horizontal LazyLists.
  }

  // TODO Dynamically update width and height of UIViewLazyList when set
  override fun width(width: Constraint) {}

  override fun height(height: Constraint) {}

  override fun margin(margin: Margin) {
    tableView.contentInset = UIEdgeInsetsMake(
      margin.top.value,
      margin.start.value,
      margin.end.value,
      margin.bottom.value,
    )
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
    // TODO Support CrossAxisAlignment in `redwood-lazylayout-uiview`
  }

  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) {
    if (scrollItemIndex.index < processor.size) {
      tableView.scrollToRowAtIndexPath(
        NSIndexPath.indexPathForItem(scrollItemIndex.index.toLong(), 0),
        UITableViewScrollPosition.UITableViewScrollPositionTop,
        animated = false,
      )
    }
  }

  override fun itemsBefore(itemsBefore: Int) {
    processor.itemsBefore(itemsBefore)
  }

  override fun itemsAfter(itemsAfter: Int) {
    processor.itemsAfter(itemsAfter)
  }

  override fun onEndChanges() {
    processor.onEndChanges()
  }
}

private const val reuseIdentifier = "LazyListContainerCell"

internal class LazyListContainerCell(
  style: UITableViewCellStyle,
  reuseIdentifier: String?,
) : UITableViewCell(style, reuseIdentifier) {
  internal var cell: LazyListUpdateProcessor.Cell<LazyListContainerCell, UIView>? = null
  internal var widgetView: UIView? = null

  override fun initWithStyle(
    style: UITableViewCellStyle,
    reuseIdentifier: String?,
  ): UITableViewCell = LazyListContainerCell(style, reuseIdentifier)

  override fun initWithFrame(
    frame: CValue<CGRect>,
  ): UITableViewCell {
    return LazyListContainerCell(UITableViewCellStyle.UITableViewCellStyleDefault, null)
      .apply { setFrame(frame) }
  }

  override fun willMoveToSuperview(newSuperview: UIView?) {
    super.willMoveToSuperview(newSuperview)

    // Confirm the cell is bound when it's about to be displayed.
    if (superview == null && newSuperview != null) {
      require(cell!!.isBound) { "about to display a cell that isn't bound!" }
    }

    // Unbind the cell when its view is detached from the table.
    if (superview != null && newSuperview == null) {
      removeAllSubviews()
      cell?.unbind()
      cell = null
    }
  }

  override fun prepareForReuse() {
    super.prepareForReuse()
    removeAllSubviews()
    cell?.unbind()
    cell = null
  }

  fun setWidget(widgetView: UIView) {
    this.widgetView = widgetView

    removeAllSubviews()
    contentView.addSubview(widgetView)
    contentView.translatesAutoresizingMaskIntoConstraints = false
    setNeedsLayout()
  }

  override fun layoutSubviews() {
    super.layoutSubviews()

    val widgetView = this.widgetView ?: return
    widgetView.setFrame(bounds)
    contentView.setFrame(bounds)
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    return widgetView?.sizeThatFits(size) ?: return super.sizeThatFits(size)
  }

  private fun removeAllSubviews() {
    contentView.subviews.forEach {
      (it as UIView).removeFromSuperview()
    }
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
      if (tableView.refreshControl != refreshControl) {
        tableView.refreshControl = refreshControl
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
