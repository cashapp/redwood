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

import app.cash.redwood.layout.widget.Box
import app.cash.redwood.layout.widget.Column
import app.cash.redwood.layout.widget.RedwoodLayoutWidgetFactory
import app.cash.redwood.layout.widget.Row
import app.cash.redwood.layout.widget.Spacer
import platform.UIKit.UIView

@ObjCName("UIViewRedwoodLayoutWidgetFactory", exact = true)
public class UIViewRedwoodLayoutWidgetFactory : RedwoodLayoutWidgetFactory<UIView> {
  override fun Box(): Box<UIView> = UIViewBox()
  override fun Column(): Column<UIView> = UIViewColumn()
  override fun Row(): Row<UIView> = UIViewRow()
  override fun Spacer(): Spacer<UIView> = UIViewSpacer()
}
