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
package app.cash.redwood.lazylayout.dom

import app.cash.redwood.layout.api.Constraint
import app.cash.redwood.ui.Density
import app.cash.redwood.ui.Dp
import kotlin.math.roundToInt
import org.w3c.dom.css.CSSStyleDeclaration

internal fun Dp.toPxString(): String = with(Density(1.0)) {
  "${toPx().roundToInt()}px"
}

internal fun Constraint.toCss() = when (this) {
  Constraint.Wrap -> "auto"
  Constraint.Fill -> "100%"
  else -> throw AssertionError()
}

internal var CSSStyleDeclaration.marginInlineStart: String
  get() = this.getPropertyValue("margin-inline-start")
  set(value) {
    this.setProperty("margin-inline-start", value)
  }

internal var CSSStyleDeclaration.marginInlineEnd: String
  get() = this.getPropertyValue("margin-inline-end")
  set(value) {
    this.setProperty("margin-inline-end", value)
  }
