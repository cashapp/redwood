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
package app.cash.redwood.treehouse

import app.cash.redwood.Modifier
import app.cash.redwood.widget.Widget
import kotlinx.cinterop.cValue
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIView

internal class EmptyDynamicContentWidgetFactory : DynamicContentWidgetFactory<UIView> {
  override fun Loading(): Loading<UIView> = EmptyLoading()

  override fun Crashed(): Crashed<UIView> = EmptyCrashed()

  class EmptyLoading : Loading<UIView> {
    override val value: UIView = UIView(cValue { CGRectZero })
    override var modifier: Modifier = Modifier
    override val allChildren: List<Widget.Children<UIView>>
      get() = listOf()
  }

  class EmptyCrashed : Crashed<UIView> {
    override val value: UIView = UIView(cValue { CGRectZero })
    override var modifier: Modifier = Modifier
    override val allChildren: List<Widget.Children<UIView>>
      get() = listOf()
    override fun uncaughtException(uncaughtException: Throwable) {
    }

    override fun restart(restart: () -> Unit) {
    }
  }
}
