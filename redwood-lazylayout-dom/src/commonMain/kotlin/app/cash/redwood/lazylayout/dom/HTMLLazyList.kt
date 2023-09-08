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
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import app.cash.redwood.ui.Margin
import app.cash.redwood.widget.HTMLElementChildren
import app.cash.redwood.widget.MutableListChildren
import org.w3c.dom.Document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

internal open class HTMLLazyList(document: Document) : LazyList<HTMLElement> {
  override var modifier: Modifier = Modifier

  final override val placeholder = MutableListChildren<HTMLElement>()

  final override val value = document.createElement("div") as HTMLDivElement

  init {
    value.style.display = "flex"
  }

  final override val items = HTMLElementChildren(value)

  override fun width(width: Constraint) {
  }

  override fun height(height: Constraint) {
  }

  override fun margin(margin: Margin) {
  }

  override fun crossAxisAlignment(crossAxisAlignment: CrossAxisAlignment) {
  }

  override fun scrollItemIndex(scrollItemIndex: ScrollItemIndex) {
  }

  override fun isVertical(isVertical: Boolean) {
    value.style.flexDirection = if (isVertical) "column" else "row"
  }

  override fun onViewportChanged(onViewportChanged: (firstVisibleItemIndex: Int, lastVisibleItemIndex: Int) -> Unit) {
  }

  override fun itemsBefore(itemsBefore: Int) {
  }

  override fun itemsAfter(itemsAfter: Int) {
  }
}

internal class HTMLRefreshableLazyList(
  document: Document,
) : HTMLLazyList(document), RefreshableLazyList<HTMLElement> {
  override fun refreshing(refreshing: Boolean) {
  }

  override fun onRefresh(onRefresh: (() -> Unit)?) {
  }

  override fun pullRefreshContentColor(pullRefreshContentColor: UInt) {
  }
}
