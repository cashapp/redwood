/*
 * Copyright (C) 2024 Square, Inc.
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
package app.cash.redwood.snapshot.testing

import app.cash.redwood.Modifier
import app.cash.redwood.ui.Dp

interface TestWidgetFactory<W : Any> {
  fun color(): Color<W>
  fun text(): Text<W>
  fun column(): SimpleColumn<W>
  fun scrollWrapper(): ScrollWrapper<W>
}

fun <W : Any> TestWidgetFactory<W>.text(
  text: String,
  modifier: Modifier = Modifier,
  backgroundColor: Int = Green,
): Text<W> = text().apply {
  this.text(text)
  this.bgColor(backgroundColor)
  this.modifier = modifier
}

fun <W : Any> TestWidgetFactory<W>.color(
  color: Int,
  width: Dp,
  height: Dp,
  modifier: Modifier = Modifier,
) = color().apply {
  this.color(color)
  this.width(width)
  this.height(height)
  this.modifier = modifier
}
