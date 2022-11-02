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
package app.cash.redwood.layout.uiview

import kotlinx.cinterop.CValue
import platform.CoreGraphics.CGSize
import platform.UIKit.UIScrollView

/**
 * A delegate whose methods should be called when the corresponding [UIScrollView] method is invoked.
 */
public interface RedwoodUIScrollViewDelegate {
  public val intrinsicContentSize: CValue<CGSize>
  public fun sizeThatFits(size: CValue<CGSize>): CValue<CGSize>
  public fun layoutSubviews()
}

/**
 * A factory that creates [UIScrollView] subclasses that delegate to a [RedwoodUIScrollViewDelegate].
 */
public interface RedwoodUIScrollViewFactory {
  public fun create(delegate: RedwoodUIScrollViewDelegate): UIScrollView
}
