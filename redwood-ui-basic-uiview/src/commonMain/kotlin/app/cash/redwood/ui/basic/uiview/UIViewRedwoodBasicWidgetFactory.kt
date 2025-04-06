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
package app.cash.redwood.ui.basic.uiview

import app.cash.redwood.layout.uiview.UIViewRedwoodLayoutWidgetFactory
import app.cash.redwood.lazylayout.uiview.UIViewRedwoodLazyLayoutWidgetFactory
import app.cash.redwood.ui.basic.modifier.Reuse
import app.cash.redwood.ui.basic.widget.Button
import app.cash.redwood.ui.basic.widget.Image
import app.cash.redwood.ui.basic.widget.RedwoodUiBasicWidgetFactory
import app.cash.redwood.ui.basic.widget.RedwoodUiBasicWidgetSystem
import app.cash.redwood.ui.basic.widget.Text
import app.cash.redwood.ui.basic.widget.TextInput
import platform.UIKit.UIView

@ObjCName("UIViewRedwoodUiBasicWidgetFactory", exact = true)
public class UIViewRedwoodUiBasicWidgetFactory : RedwoodUiBasicWidgetFactory<UIView> {
  private val imageLoader = RemoteImageLoader()

  override fun TextInput(): TextInput<UIView> = UIViewTextInput()
  override fun Text(): Text<UIView> = UIViewText()
  override fun Image(): Image<UIView> = UIViewImage(imageLoader)
  override fun Button(): Button<UIView> = UIViewButton()

  override fun Reuse(value: UIView, modifier: Reuse) {}
}

@Suppress("FunctionName") // Acting like a type.
public fun UIViewRedwoodUiBasicWidgetSystem(): RedwoodUiBasicWidgetSystem<UIView> {
  return RedwoodUiBasicWidgetSystem(
    RedwoodUiBasic = UIViewRedwoodUiBasicWidgetFactory(),
    RedwoodLayout = UIViewRedwoodLayoutWidgetFactory(),
    RedwoodLazyLayout = UIViewRedwoodLazyLayoutWidgetFactory(),
  )
}
