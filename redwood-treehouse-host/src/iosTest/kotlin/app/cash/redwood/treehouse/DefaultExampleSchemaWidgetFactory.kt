/*
 * Copyright (C) 2023 Square, Inc.
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

import example.redwood.widget.Button
import example.redwood.widget.Button2
import example.redwood.widget.ExampleSchemaWidgetFactory
import example.redwood.widget.Row
import example.redwood.widget.ScopedRow
import example.redwood.widget.Space
import example.redwood.widget.Text
import example.redwood.widget.TextInput
import platform.UIKit.UIView

class DefaultExampleSchemaWidgetFactory : ExampleSchemaWidgetFactory<UIView> {
  override fun Row(): Row<UIView> = RowBinding()
  override fun ScopedRow(): ScopedRow<UIView> = TODO("Not yet implemented")
  override fun Text(): Text<UIView> = TextBinding()
  override fun Button(): Button<UIView> = TODO("Not yet implemented")
  override fun Button2(): Button2<UIView> = TODO("Not yet implemented")
  override fun TextInput(): TextInput<UIView> = TODO("Not yet implemented")
  override fun Space(): Space<UIView> = TODO("Not yet implemented")
}
