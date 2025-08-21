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
package app.cash.redwood.ui.core.uiview

import app.cash.redwood.ui.core.modifier.FocusRequester
import app.cash.redwood.ui.core.widget.RedwoodUiCoreWidgetFactory
import platform.UIKit.UIView

@ObjCName("UIViewRedwoodUiCoreWidgetFactory", exact = true)
public class UIViewRedwoodUiCoreWidgetFactory : RedwoodUiCoreWidgetFactory<UIView> {
  override fun FocusRequester(
    value: UIView,
    modifier: FocusRequester,
  ) {
  }
}
