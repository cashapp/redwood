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
@file:Suppress("FunctionName")

package app.cash.redwood.lazylayout.dom

import app.cash.redwood.Modifier
import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.layout.api.CrossAxisAlignment
import app.cash.redwood.lazylayout.api.ScrollItemIndex
import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.LazyListScrollProcessor
import app.cash.redwood.lazylayout.widget.LazyListUpdateProcessor
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.Widget
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

internal class HTMLLazyList(document: Document) : LazyList<HTMLElement> {
  override var modifier: Modifier = Modifier

  override val value = document.createElement("div") as HTMLDivElement

  private val processor = object : LazyListUpdateProcessor<HTMLElement, HTMLElement>() {
    override fun insertRows(index: Int, count: Int) {
    }

    override fun deleteRows(index: Int, count: Int) {
    }

    override fun setContent(view: HTMLElement, widget: Widget<HTMLElement>?) {
      if (widget != null) {
        view.appendChild(widget.value)
      }
    }
  }

  private val scrollProcessor = object : LazyListScrollProcessor() {
    override fun contentSize(): Int = processor.size

    override fun programmaticScroll(firstIndex: Int, animated: Boolean) {
      TODO("Not yet implemented")
    }
  }

  private val visibleSet = mutableSetOf<Element>()
  private var highestEverVisibleIndex = 0

  private val intersectionObserver = IntersectionObserver(
    { entries, _ ->
      entries.forEach { entry ->
        if (entry.isIntersecting) {
          visibleSet.add(entry.target)
        } else {
          visibleSet.remove(entry.target)
        }
      }

      val highestVisibleIndex = items.widgets.indexOfLast { it.value in visibleSet }
      if (highestVisibleIndex > highestEverVisibleIndex) {
        highestEverVisibleIndex = highestVisibleIndex
        // We currently won't unload any items that have been scrolled out of view
        scrollProcessor.onUserScroll(firstIndex = 0, lastIndex = highestEverVisibleIndex)
      }
    },
  )

  init {
    value.style.display = "flex"
  }

  override val items: Widget.Children<HTMLElement> = object : Widget.Children<HTMLElement> by processor.items {
    override fun insert(index: Int, widget: Widget<HTMLElement>) {
      processor.items.insert(index, widget)
      intersectionObserver.observe(widget.value)

      // Null element returned when index == childCount causes insertion at end.
      val current = value.children[index]
      value.insertBefore(widget.value, current)
    }

    override fun remove(index: Int, count: Int) {
      for (i in index..<index + count) {
        intersectionObserver.unobserve(value.children[i]!!)
      }
      processor.items.remove(index, count)

      repeat(count) {
        value.removeChild(value.children[index]!!)
      }
    }
  }

  override val placeholder = processor.placeholder

  override fun width(width: Constraint) {
    value.style.width = width.toCss()
  }

  override fun height(height: Constraint) {
    value.style.height = height.toCss()
  }

  override fun margin(margin: Margin) {
    value.style.apply {
      marginInlineStart = margin.start.toPxString()
      marginInlineEnd = margin.end.toPxString()
      marginTop = margin.top.toPxString()
      marginBottom = margin.bottom.toPxString()
    }
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
  }

  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) {
    scrollProcessor.scrollItemIndex(scrollItemIndex)
  }

  override fun isVertical(isVertical: Boolean) {
    value.style.apply {
      flexDirection = if (isVertical) "column" else "row"
      if (isVertical) {
        overflowY = "scroll"
        removeProperty("overflowX")
      } else {
        overflowX = "scroll"
        removeProperty("overflowY")
      }
    }
  }

  override fun onViewportChanged(onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit) {
    scrollProcessor.onViewportChanged(onViewportChanged)
  }

  override fun itemsBefore(itemsBefore: Int) {
    processor.itemsBefore(itemsBefore)
  }

  override fun itemsAfter(itemsAfter: Int) {
    processor.itemsAfter(itemsAfter)
  }
}

internal class HTMLRefreshableLazyList(
  document: Document,
) : RefreshableLazyList<HTMLElement> {
  private val delegate = HTMLLazyList(document)

  override val value get() = delegate.value
  override var modifier by delegate::modifier

  override val placeholder get() = delegate.placeholder
  override val items get() = delegate.items

  override fun isVertical(isVertical: Boolean) = delegate.isVertical(isVertical)
  override fun onViewportChanged(onViewportChanged: (Int, Int) -> Unit) = delegate.onViewportChanged(onViewportChanged)
  override fun itemsAfter(itemsAfter: Int) = delegate.itemsAfter(itemsAfter)
  override fun itemsBefore(itemsBefore: Int) = delegate.itemsBefore(itemsBefore)
  override fun width(width: Constraint) = delegate.width(width)
  override fun height(height: Constraint) = delegate.height(height)
  override fun margin(margin: Margin) = delegate.margin(margin)
  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) = delegate.crossAxisAlignment(crossAxisAlignment)
  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) = delegate.scrollItemIndex(scrollItemIndex)

  override fun refreshing(refreshing: Boolean) {
  }

  override fun onRefresh(onRefresh: (() -> Unit)?) {
  }

  override fun pullRefreshContentColor(pullRefreshContentColor: UInt) {
  }
}
