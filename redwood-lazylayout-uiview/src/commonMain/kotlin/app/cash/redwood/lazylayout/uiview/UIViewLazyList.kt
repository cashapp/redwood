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
import app.cash.redwood.widget.MutableListChildren
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGFloat
import platform.Foundation.NSIndexPath
import platform.Foundation.NSThread
import platform.Foundation.classForCoder
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIRefreshControl
import platform.UIKit.UIScrollView
import platform.UIKit.UITableView
import platform.UIKit.UITableViewCell
import platform.UIKit.UITableViewCellStyle
import platform.UIKit.UITableViewCellStyle.UITableViewCellStyleDefault
import platform.UIKit.UITableViewDataSourceProtocol
import platform.UIKit.UITableViewDelegateProtocol
import platform.UIKit.UITableViewRowAnimationAutomatic
import kotlin.native.concurrent.ensureNeverFrozen
import kotlinx.cinterop.cstr
import platform.UIKit.UITableViewRowAnimationNone
import platform.UIKit.UIView
import platform.UIKit.indexPathForRow
import platform.UIKit.item
import platform.UIKit.row
import platform.UIKit.section
import platform.darwin.NSInteger
import platform.darwin.NSObject

private const val reuseIdentifier = "cell"

internal open class UIViewLazyList() : LazyList<UIView> {

  private val itemsList = mutableListOf<Widget<UIView>>()

  override val placeholder: Widget.Children<UIView> = MutableListChildren()

  override val items: Widget.Children<UIView> = object : Widget.Children<UIView> {

    override fun insert(index: Int, widget: Widget<UIView>) {
      println("🟢 ${isMainThread()} insert: $index ${widget.value.hash} listSize: ${itemsList.size}")
      itemsList.add(index, widget)
      tableView.insertRowsAtIndexPaths(listOf((index).toNSIndexPath()), withRowAnimation = UITableViewRowAnimationNone)
//      tableView.performBatchUpdates(updates = {
//        tableView.insertRowsAtIndexPaths(listOf(indexPath), withRowAnimation = UITableViewRowAnimationNone)
//      },
//        completion = null
//      )

//      tableView.performBatchUpdates(updates = {
//        tableView.reloadData()
//      },
//        completion = null
//      )
//      tableView.reloadData()
    }

    override fun move(fromIndex: Int, toIndex: Int, count: Int) {
      println("🟣 t${isMainThread()} move: $fromIndex to $fromIndex count: $fromIndex listSize: ${itemsList.size}")
      itemsList.move(fromIndex, toIndex, count)

      var fromIndexPath = (itemsBefore + fromIndex).toNSIndexPath()
      var toIndexPath = (itemsBefore + toIndex).toNSIndexPath()

//      tableView.performBatchUpdates(updates = {
//        tableView.moveRowAtIndexPath(fromIndexPath, toIndexPath = toIndexPath)
//      },
//        completion = null
//      )

//      tableView.performBatchUpdates(updates = {
//        tableView.reloadData()
//      },
//        completion = null
//      )
      tableView.reloadData()
    }

    override fun remove(index: Int, count: Int) {
      println("🔴 ${isMainThread()} remove: $index count: $count listSize: ${itemsList.size}")
      itemsList.remove(index, count)
      var indexPath = (itemsBefore + index).toNSIndexPath()

      //tableView.deleteRowsAtIndexPaths(listOf(indexPath), withRowAnimation = UITableViewRowAnimationNone)

//      tableView.performBatchUpdates(updates = {
//          tableView.deleteRowsAtIndexPaths(listOf(indexPath), withRowAnimation = UITableViewRowAnimationNone)
//        },
//        completion = null
//      )

//      tableView.performBatchUpdates(updates = {
//        tableView.reloadData()
//      },
//        completion = null
//      )

      //tableView.reloadData()
    }

    override fun onModifierUpdated() {
    }
  }

  private var first: Int = 0
  private var last: Int = 0

  private val tableViewDelegate: UITableViewDelegateProtocol =
    object : NSObject(), UITableViewDelegateProtocol {
      override fun tableView(tableView: UITableView, heightForRowAtIndexPath: NSIndexPath): CGFloat {
        //println("🟠 heightForRowAtIndexPath ${heightForRowAtIndexPath.row}")

        val index = heightForRowAtIndexPath.item.toInt() - itemsBefore
        val item = itemsList[index]
        return item.value.intrinsicContentSize.useContents { this.height }
      }

      override fun scrollViewDidScroll(scrollView: UIScrollView) {
        var visiblePaths = tableView.indexPathsForVisibleRows as List<NSIndexPath>

        if (visiblePaths.isNotEmpty()) {

          // Only update this if there's a change since the last time.

          val newFirst = visiblePaths.first().row.toInt()
          val newLast = visiblePaths.last().row.toInt()

          if (newFirst != first || newLast != last) {
            println("⚪️ ${isMainThread()} scrollViewDidScroll itemsBefore: $itemsBefore count: ${itemsList.count()} f: $first l: $last listSize: ${itemsList.size}")
            first = newFirst
            last = newLast
            onViewportChanged?.invoke(newFirst, newLast)
          }
        }
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
      ): NSInteger {
        val count = itemsList.size.toLong()// + itemsAfter
        println("🟠 ${isMainThread()} numberOfRowsInSection count: $count itemsBefore: $itemsBefore listSize: ${itemsList.size} itemsAfter: $itemsAfter")
        return count
      }

      override fun tableView(
        tableView: UITableView,
        cellForRowAtIndexPath: NSIndexPath,
      ): UITableViewCell {
        println("🟠 ${isMainThread()} cellForRowAtIndexPath ${cellForRowAtIndexPath.row}")
        val cell = tableView.dequeueReusableCellWithIdentifier(reuseIdentifier) as Cell
        val index = cellForRowAtIndexPath.item.toInt() - itemsBefore
        val content = itemsList[index]

        content?.apply {
          cell.setView(this.value)
        }

        return cell
      }
    }

  internal val tableView = UITableView()
    .apply {
      dataSource = tableViewDataSource
      delegate = tableViewDelegate
      rowHeight = 10.0 // TODO: size rows by their content.
      prefetchingEnabled = true
      registerClass(
        Cell(UITableViewCellStyleDefault, reuseIdentifier).classForCoder() as ObjCClass?,
        forCellReuseIdentifier = reuseIdentifier,
      )
    }

  private var onViewportChanged: ((firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit)? = null
  private var itemsBefore = 0
  private var itemsAfter = 0

  override fun isVertical(isVertical: Boolean) {
    if (!isVertical) {
      // TODO UITableView only supports vertical scrolling. Switch to UICollectionView.
      TODO()
    }
  }

  override fun onViewportChanged(onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit) {
    this.onViewportChanged = onViewportChanged
  }

  override fun itemsBefore(itemsBefore: Int) {
    println("🟤 ${isMainThread()} itemsBefore: $itemsBefore")

    this.itemsBefore = itemsBefore
  }

  override fun itemsAfter(itemsAfter: Int) {
    println("🟤 ${isMainThread()} itemsAfter: $itemsAfter")
    val delta = itemsAfter - this.itemsAfter
    this.itemsAfter = itemsAfter

//    val positionStart = this.itemsBefore + itemsList.size
//    if (delta > 0) {
//      tableView.performBatchUpdates(updates = {
//        var indexes = (positionStart..delta).map { it.toNSIndexPath() }.toList()
//        tableView.insertRowsAtIndexPaths(indexes, withRowAnimation = UITableViewRowAnimationNone)
//      }, completion = null
//      )
//     //adapter.notifyItemRangeInserted(positionStart, delta)
//    } else if (delta < 0) {
//      tableView.performBatchUpdates(updates = {
//        var indexes = (positionStart..-delta).map { it.toNSIndexPath() }.toList()
//        tableView.deleteRowsAtIndexPaths(indexes, withRowAnimation = UITableViewRowAnimationNone)
//      }, completion = null
//      )
//      //adapter.notifyItemRangeRemoved(positionStart, -delta)
  // }
  }

  // TODO Dynamically update width and height of UIViewLazyList when set
  override fun width(width: Constraint) {
  }

  override fun height(height: Constraint) {
  }

  override var modifier: Modifier = Modifier

  override val value: UIView get() = tableView
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
}

private class Cell(
  style: UITableViewCellStyle,
  reuseIdentifier: String?,
) : UITableViewCell(style, reuseIdentifier) {
  private var view: UIView? = null

  /** Factory function for a new cell. */
  override fun initWithStyle(
    style: UITableViewCellStyle,
    reuseIdentifier: String?,
  ): UITableViewCell = Cell(style, reuseIdentifier)

  override fun prepareForReuse() {
    super.prepareForReuse()
    this.view?.removeFromSuperview()
    this.view = null
  }

  fun setView(view: UIView) {
    println("⚪️ ${isMainThread()} setView")
    this.view = view
    contentView.addSubview(view)
    view.setFrame(bounds)
  }

  override fun layoutSubviews() {
    super.layoutSubviews()
    view?.setFrame(bounds)
  }
}

// Debugging
private fun Int.toNSIndexPath(): NSIndexPath {
  return NSIndexPath.indexPathForRow(this.toLong(), inSection = 0L)
}

private fun isMainThread(): String {
  if (NSThread.isMainThread) {
    return "✔️"
  } else {
    return "❌"
  }
}
