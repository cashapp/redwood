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
package app.cash.redwood.layout.dom

import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RedwoodLazyLayoutWidgetFactory
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import org.w3c.dom.Document
import org.w3c.dom.HTMLElement

public class HTMLElementRedwoodLazyLayoutWidgetFactory(
  private val document: Document,
) : RedwoodLazyLayoutWidgetFactory<HTMLElement> {
  override fun LazyList(): LazyList<HTMLElement> = HTMLLazyList(document)

  override fun RefreshableLazyList(): RefreshableLazyList<HTMLElement> =
    HTMLRefreshableLazyList(document)
}
