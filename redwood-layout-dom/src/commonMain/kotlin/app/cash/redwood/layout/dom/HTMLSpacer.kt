/*
 * Copyright (C) 2025 Square, Inc.
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

import app.cash.redwood.Modifier
import app.cash.redwood.layout.widget.Spacer
import app.cash.redwood.ui.Dp
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

internal class HTMLSpacer(
  override val value: HTMLDivElement,
) : Spacer<HTMLElement> {
  override var modifier: Modifier = Modifier

  override fun width(width: Dp) {
    value.style.width = width.toPxString()
  }

  override fun height(height: Dp) {
    value.style.height = height.toPxString()
  }
}
