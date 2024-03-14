/*
 * Copyright (C) 2021 Square, Inc.
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
package com.example.redwood.counter.browser

import app.cash.redwood.compose.RedwoodComposition
import app.cash.redwood.compose.WindowAnimationFrameClock
import app.cash.redwood.layout.dom.HTMLElementRedwoodLayoutWidgetFactory
import app.cash.redwood.widget.asRedwoodView
import com.example.redwood.counter.presenter.Counter
import com.example.redwood.counter.widget.SchemaWidgetSystem
import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.plus
import org.w3c.dom.HTMLElement

fun main() {
  val content = document.getElementById("content") as HTMLElement

  @OptIn(DelicateCoroutinesApi::class)
  val composition = RedwoodComposition(
    scope = GlobalScope + WindowAnimationFrameClock,
    view = content.asRedwoodView(),
    widgetSystem = SchemaWidgetSystem(
      Schema = HtmlWidgetFactory(document),
      RedwoodLayout = HTMLElementRedwoodLayoutWidgetFactory(document),
    ),
  )
  composition.setContent {
    Counter()
  }
}
