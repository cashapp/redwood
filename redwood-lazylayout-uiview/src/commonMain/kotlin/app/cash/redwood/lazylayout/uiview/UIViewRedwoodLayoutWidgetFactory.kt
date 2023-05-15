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
package app.cash.redwood.lazylayout.uiview

import app.cash.redwood.lazylayout.widget.LazyList
import app.cash.redwood.lazylayout.widget.RedwoodLazyLayoutWidgetFactory
import app.cash.redwood.lazylayout.widget.RefreshableLazyList
import platform.UIKit.UIRefreshControl
import platform.UIKit.UIView

@ObjCName("UIViewRedwoodLazyLayoutWidgetFactory", exact = true)
public class UIViewRedwoodLazyLayoutWidgetFactory : RedwoodLazyLayoutWidgetFactory<UIView> {
  override fun LazyList(): LazyList<UIView> = UIViewLazyList()

  override fun RefreshableLazyList(): RefreshableLazyList<UIView> = UIViewRefreshableLazyList(
    refreshControlFactory = { UIRefreshControl() },
  )
}
