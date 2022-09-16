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
package app.cash.redwood

import app.cash.redwood.Node.Companion.DefaultFlexBasisPercent
import app.cash.redwood.Node.Companion.DefaultFlexGrow
import app.cash.redwood.Node.Companion.DefaultFlexShrink
import app.cash.redwood.Node.Companion.DefaultOrder
import app.cash.redwood.Node.Companion.WrapContent

class FakeNode(
  override val width: Int = WrapContent,
  override val height: Int = WrapContent,
  override val minWidth: Int = 0,
  override val minHeight: Int = 0,
  override val maxWidth: Int = Int.MAX_VALUE,
  override val maxHeight: Int = Int.MAX_VALUE,
  override val visible: Boolean = true,
  override val baseline: Int = -1,
  override val order: Int = DefaultOrder,
  override val flexGrow: Float = DefaultFlexGrow,
  override val flexShrink: Float = DefaultFlexShrink,
  override val flexBasisPercent: Float = DefaultFlexBasisPercent,
  override val alignSelf: AlignSelf = AlignSelf.Auto,
  override val wrapBefore: Boolean = false,
  override val margin: Spacing = Spacing.Zero,
) : Node {

  override var measuredWidth: Int = -1
  override var measuredHeight: Int = -1

  override fun measure(widthSpec: MeasureSpec, heightSpec: MeasureSpec) {
    measuredWidth = MeasureSpec.resolveSize(width, widthSpec)
    measuredHeight = MeasureSpec.resolveSize(height, heightSpec)
  }

  override fun layout(left: Int, top: Int, right: Int, bottom: Int) {}
}
