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
import app.cash.redwood.lazylayout.widget.LazyListScrollProcessor
import app.cash.redwood.lazylayout.widget.LazyListUpdateProcessor
import app.cash.redwood.lazylayout.widget.LazyListUpdateProcessor.Binding
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.ChangeListener
import app.cash.redwood.widget.ResizableWidget
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
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
import platform.UIKit.UITableViewStyle
import platform.UIKit.UIView
import platform.UIKit.indexPathForItem
import platform.UIKit.item
import platform.darwin.NSInteger
import platform.darwin.NSObject

internal class UIViewLazyList :
  LazyList<UIView>,
  ChangeListener {
  internal var tableView: UITableView? = object : UITableView(
    CGRectZero.readValue(),
    UITableViewStyle.UITableViewStylePlain,
  ) {
    override fun setContentOffset(contentOffset: CValue<CGPoint>, animated: Boolean) {
      val scrollProcessor = this@UIViewLazyList.scrollProcessor ?: return // Detached.

      // If the caller is requesting a contentOffset with y == 0,
      // and the current contentOffset.y is not 0,
      // assume that it's a programmatic scroll-to-top call.
      if (contentOffset.useContents { y } == 0.0 && this.contentOffset.useContents { y } != 0.0) {
        ignoreScrollUpdates = true
        scrollProcessor.onScrollToTop()
      }
      super.setContentOffset(contentOffset, animated)
    }
  }

  override var modifier: Modifier = Modifier

  override val value: UIView
    get() = tableView ?: error("detached")

  private var onRefresh: (() -> Unit)? = null

  private var refreshControl: UIRefreshControl? = null

  fun requireRefreshControl(): UIRefreshControl {
    val result = refreshControl
    if (result != null) return result

    return UIRefreshControl()
      .apply {
        setEventHandler(UIControlEventValueChanged) {
          onRefresh?.invoke()
        }
      }
      .also { this.refreshControl = it }
  }

  fun onRefresh(onRefresh: (() -> Unit)?) {
    val tableView = this.tableView ?: error("detached")
    this.onRefresh = onRefresh

    if (onRefresh != null) {
      if (tableView.refreshControl != refreshControl) {
        tableView.refreshControl = refreshControl
      }
    } else {
      refreshControl?.removeFromSuperview()
    }
  }

  internal inner class UpdateProcessor : LazyListUpdateProcessor<LazyListContainerCell, UIView>() {
    override fun createPlaceholder(original: UIView): UIView = SizeOnlyPlaceholder(original)

    override fun insertRows(index: Int, count: Int) {
      rowCount += count
      val tableView = this@UIViewLazyList.tableView ?: error("detached")

      // TODO(jwilson): pass a range somehow when 'count' is large?
      tableView.beginUpdates()
      UIView.performWithoutAnimation {
        tableView.insertRowsAtIndexPaths(
          (index until index + count).map { NSIndexPath.indexPathForItem(it.convert(), 0) },
          UITableViewRowAnimationNone,
        )
      }
      tableView.endUpdates()
    }

    override fun deleteRows(index: Int, count: Int) {
      rowCount -= count
      val tableView = this@UIViewLazyList.tableView ?: error("detached")

      // TODO(jwilson): pass a range somehow when 'count' is large?
      tableView.beginUpdates()
      UIView.performWithoutAnimation {
        tableView.deleteRowsAtIndexPaths(
          (index until index + count).map { NSIndexPath.indexPathForItem(it.convert(), 0) },
          UITableViewRowAnimationNone,
        )
      }
      tableView.endUpdates()
    }

    override fun setContent(view: LazyListContainerCell, widget: Widget<UIView>?) {
      view.setContent(widget)
    }

    override fun detach(view: LazyListContainerCell) {
      view.detach()
    }

    override fun detach() {
      this@UIViewLazyList.detach()
    }

    fun invalidateSize(binding: Binding<LazyListContainerCell, UIView>) {
      val tableView = this@UIViewLazyList.tableView ?: return
      val lazyListContainerCell = binding.view ?: return
      val indexPath = tableView.indexPathForCell(lazyListContainerCell) ?: return

      tableView.reloadRowsAtIndexPaths(
        listOf(indexPath),
        UITableViewRowAnimationNone,
      )
    }
  }

  private var updateProcessor: UpdateProcessor? = UpdateProcessor()

  /** Cache of [LazyListUpdateProcessor.size] so we can return it after [detach]. */
  private var rowCount = 0

  private var ignoreScrollUpdates = false

  private var scrollProcessor: LazyListScrollProcessor? = object : LazyListScrollProcessor() {
    override fun contentSize() = rowCount

    override fun programmaticScroll(firstIndex: Int, animated: Boolean) {
      val tableView = this@UIViewLazyList.tableView ?: error("detached")
      ignoreScrollUpdates = animated // Don't forward scroll updates to scrollProcessor.
      tableView.scrollToRowAtIndexPath(
        NSIndexPath.indexPathForItem(firstIndex.toLong(), 0),
        UITableViewScrollPosition.UITableViewScrollPositionTop,
        animated = animated,
      )
    }
  }

  override val placeholder: Widget.Children<UIView> = updateProcessor!!.placeholder

  override val items: Widget.Children<UIView> = updateProcessor!!.items

  private val dataSource = object : NSObject(), UITableViewDataSourceProtocol {
    override fun tableView(
      tableView: UITableView,
      numberOfRowsInSection: NSInteger,
    ): Long {
      require(numberOfRowsInSection == 0L)
      return rowCount.toLong()
    }

    override fun tableView(
      tableView: UITableView,
      cellForRowAtIndexPath: NSIndexPath,
    ): UITableViewCell {
      val updateProcessor = this@UIViewLazyList.updateProcessor
        ?: return UITableViewCell(UITableViewCellStyle.UITableViewCellStyleDefault, null) // Detached.
      val index = cellForRowAtIndexPath.item.toInt()
      return updateProcessor.getOrCreateView(index) { binding ->
        createView(tableView, binding, index)
      }
    }

    private fun createView(
      tableView: UITableView,
      binding: Binding<LazyListContainerCell, UIView>,
      index: Int,
    ): LazyListContainerCell {
      val result = tableView.dequeueReusableCellWithIdentifier(
        identifier = REUSE_IDENTIFIER,
        forIndexPath = NSIndexPath.indexPathForItem(index.convert(), 0.convert()),
      ) as LazyListContainerCell
      require(result.binding == null)
      result.binding = binding
      return result
    }
  }

  private val tableViewDelegate: UITableViewDelegateProtocol =
    object : NSObject(), UITableViewDelegateProtocol {
      override fun scrollViewDidScroll(scrollView: UIScrollView) {
        val tableView = this@UIViewLazyList.tableView ?: return // Detached.
        val scrollProcessor = this@UIViewLazyList.scrollProcessor ?: return // Detached.
        if (ignoreScrollUpdates) return // Only notify of user scrolls.

        val visibleIndexPaths = tableView.indexPathsForVisibleRows ?: return
        if (visibleIndexPaths.isEmpty()) return

        val firstIndex = visibleIndexPaths.minOf { (it as NSIndexPath).item.toInt() }
        val lastIndex = visibleIndexPaths.maxOf { (it as NSIndexPath).item.toInt() }
        scrollProcessor.onUserScroll(firstIndex, lastIndex)
      }

      /**
       * If the user begins a drag while we’re programmatically scrolling, well then we're not
       * programmatically scrolling anymore.
       */
      override fun scrollViewWillBeginDragging(scrollView: UIScrollView) {
        ignoreScrollUpdates = false
      }

      override fun scrollViewDidEndScrollingAnimation(scrollView: UIScrollView) {
        ignoreScrollUpdates = false
      }
    }

  init {
    tableView!!.apply {
      dataSource = this@UIViewLazyList.dataSource
      delegate = tableViewDelegate
      rowHeight = UITableViewAutomaticDimension
      separatorStyle = UITableViewCellSeparatorStyleNone
      backgroundColor = UIColor.clearColor

      registerClass(
        cellClass = LazyListContainerCell(UITableViewCellStyle.UITableViewCellStyleDefault, REUSE_IDENTIFIER)
          .initWithFrame(CGRectZero.readValue()).classForCoder() as ObjCClass?,
        forCellReuseIdentifier = REUSE_IDENTIFIER,
      )
    }
  }

  final override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) {
    val scrollProcessor = this@UIViewLazyList.scrollProcessor ?: error("detached")
    scrollProcessor.onViewportChanged(onViewportChanged)
  }

  override fun isVertical(isVertical: Boolean) {
    // TODO: support horizontal LazyLists.
  }

  // TODO Dynamically update width and height of UIViewLazyList when set
  override fun width(width: Constraint) {}

  override fun height(height: Constraint) {}

  override fun margin(margin: Margin) {
    val tableView = this@UIViewLazyList.tableView ?: error("detached")
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
    val scrollProcessor = this@UIViewLazyList.scrollProcessor ?: error("detached")
    scrollProcessor.scrollItemIndex(scrollItemIndex)
  }

  override fun itemsBefore(itemsBefore: Int) {
    val updateProcessor = this@UIViewLazyList.updateProcessor ?: error("detached")
    updateProcessor.itemsBefore(itemsBefore)
  }

  override fun itemsAfter(itemsAfter: Int) {
    val updateProcessor = this@UIViewLazyList.updateProcessor ?: error("detached")
    updateProcessor.itemsAfter(itemsAfter)
  }

  override fun onEndChanges() {
    val updateProcessor = this@UIViewLazyList.updateProcessor ?: error("detached")
    val scrollProcessor = this@UIViewLazyList.scrollProcessor ?: error("detached")
    updateProcessor.onEndChanges()
    scrollProcessor.onEndChanges()
  }

  fun detach() {
    // Break reference cycles.
    tableView = null
    updateProcessor = null
    scrollProcessor = null
    refreshControl = null
  }
}

private const val REUSE_IDENTIFIER = "LazyListContainerCell"

internal class LazyListContainerCell(
  style: UITableViewCellStyle,
  reuseIdentifier: String?,
) : UITableViewCell(style, reuseIdentifier) {
  internal var binding: Binding<LazyListContainerCell, UIView>? = null
  private var content: Widget<UIView>? = null

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

    backgroundColor = UIColor.clearColor
    if (newSuperview != null) {
      if (newSuperview is UITableView) {
        newSuperview.separatorStyle = UITableViewCellSeparatorStyleNone
      }

      // Make sure the cell is bound when it's about to be displayed.
      if (superview == null && !binding!!.isBound) {
        binding!!.bind(this)
      }
    } else {
      // Unbind the cell when its view is detached from the table.
      if (superview != null) {
        binding?.unbind()
      }
    }
  }

  override fun prepareForReuse() {
    super.prepareForReuse()
    binding?.unbind()
    binding = null
  }

  override fun layoutSubviews() {
    super.layoutSubviews()

    content?.value?.setFrame(bounds)
    contentView.setFrame(bounds)
  }

  override fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize> {
    val content = this.content ?: return super.sizeThatFits(size)
    return content.value.sizeThatFits(size)
  }

  internal fun setContent(widget: Widget<UIView>?) {
    val previous = this.content
    if (previous != null) {
      previous.value.removeFromSuperview()
      if (previous is ResizableWidget) {
        previous.sizeListener = null
      }
    }
    this.selectedBackgroundView = null

    this.content = widget
    if (widget != null) {
      contentView.addSubview(widget.value)
      if (widget is ResizableWidget) {
        widget.sizeListener = object : ResizableWidget.SizeListener {
          override fun invalidateSize() {
            val binding = binding ?: return
            (binding.processor as UIViewLazyList.UpdateProcessor).invalidateSize(binding)
          }
        }
      }
    }
    setNeedsLayout()
  }

  internal fun detach() {
    val previous = content
    if (previous is ResizableWidget) {
      previous.sizeListener = null
    }

    this.binding = null // Break a reference cycle.
    this.content = null // Break a reference cycle.
  }
}

internal class UIViewRefreshableLazyList :
  RefreshableLazyList<UIView>,
  ChangeListener {
  private val delegate = UIViewLazyList()

  override val value get() = delegate.value
  override var modifier by delegate::modifier

  override val placeholder get() = delegate.placeholder
  override val items get() = delegate.items

  override fun isVertical(isVertical: Boolean) = delegate.isVertical(isVertical)
  override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) = delegate.onViewportChanged(onViewportChanged)
  override fun itemsBefore(itemsBefore: Int) = delegate.itemsBefore(itemsBefore)
  override fun itemsAfter(itemsAfter: Int) = delegate.itemsAfter(itemsAfter)
  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) = delegate.crossAxisAlignment(crossAxisAlignment)
  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) = delegate.scrollItemIndex(scrollItemIndex)
  override fun onEndChanges() = delegate.onEndChanges()

  override fun refreshing(refreshing: Boolean) {
    val refreshControl = delegate.requireRefreshControl()

    if (refreshing != refreshControl.refreshing) {
      if (refreshing) {
        refreshControl.beginRefreshing()
      } else {
        refreshControl.endRefreshing()
      }
    }
  }

  override fun onRefresh(onRefresh: (() -> Unit)?) {
    delegate.onRefresh(onRefresh)
  }

  override fun pullRefreshContentColor(pullRefreshContentColor: UInt) {
    delegate.requireRefreshControl().tintColor = UIColor(pullRefreshContentColor)
  }
}

private fun UIColor(color: UInt): UIColor = UIColor(
  alpha = ((color and 0xFF000000u) shr 24).toDouble() / 255.0,
  red = ((color and 0x00FF0000u) shr 16).toDouble() / 255.0,
  green = ((color and 0x0000FF00u) shr 8).toDouble() / 255.0,
  blue = (color and 0x000000FFu).toDouble() / 255.0,
)

private class SizeOnlyPlaceholder(
  private val original: UIView,
) : UIView(CGRectZero.readValue()) {
  override fun sizeThatFits(size: CValue<CGSize>) = original.sizeThatFits(size)
}
