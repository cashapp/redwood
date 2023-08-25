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
package app.cash.redwood.protocol.widget

import app.cash.redwood.Modifier
import example.redwood.widget.Button
import example.redwood.widget.Button2
import example.redwood.widget.ExampleSchemaWidgetFactory
import example.redwood.widget.Row
import example.redwood.widget.ScopedRow
import example.redwood.widget.Text
import example.redwood.widget.TextInput

open class EmptyExampleSchemaWidgetFactory : ExampleSchemaWidgetFactory<Nothing> {
  override fun Row(): Row<Nothing> = TODO()
  override fun ScopedRow(): ScopedRow<Nothing> = TODO()
  override fun Text(): Text<Nothing> = TODO()
  override fun Button() = object : Button<Nothing> {
    override val value get() = TODO()
    override var modifier: Modifier
      get() = TODO()
      set(_) { TODO() }

    override fun text(text: String?) = TODO()
    override fun onClick(onClick: (() -> Unit)?) = TODO()
  }
  override fun Button2(): Button2<Nothing> = TODO()
  override fun TextInput(): TextInput<Nothing> = TODO()
}
