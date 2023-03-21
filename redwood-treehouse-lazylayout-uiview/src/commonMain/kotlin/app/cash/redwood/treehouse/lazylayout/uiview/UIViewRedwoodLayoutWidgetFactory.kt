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
package app.cash.redwood.treehouse.lazylayout.uiview

import app.cash.redwood.treehouse.AppService
import app.cash.redwood.treehouse.TreehouseApp
import app.cash.redwood.treehouse.TreehouseView.WidgetSystem
import app.cash.redwood.treehouse.lazylayout.widget.LazyColumn
import app.cash.redwood.treehouse.lazylayout.widget.RedwoodTreehouseLazyLayoutWidgetFactory
import platform.UIKit.UIView

@ObjCName("UIViewRedwoodTreehouseLazyLayoutWidgetFactory")
public class UIViewRedwoodTreehouseLazyLayoutWidgetFactory<A : AppService>(
  private var treehouseApp: TreehouseApp<A>,
  private var widgetSystem: WidgetSystem,
) : RedwoodTreehouseLazyLayoutWidgetFactory<UIView> {
  override fun LazyColumn(): LazyColumn<UIView> = UIViewLazyColumn(treehouseApp, widgetSystem)
}
